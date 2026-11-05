package com.emarketplace.notification;

import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = {"http://localhost:4200", "https://simple-marketplace-e-com.netlify.app"})
public class MarketplaceNotificationController {
    private final DirectNotificationEvents events;
    private final NotificationRepository notifications;
    private final NotificationReceiptRepository receipts;

    public MarketplaceNotificationController(DirectNotificationEvents events,
                                             NotificationRepository notifications,
                                             NotificationReceiptRepository receipts) {
        this.events = events;
        this.notifications = notifications;
        this.receipts = receipts;
    }

    @PostMapping @ResponseStatus(HttpStatus.ACCEPTED)
    public void send(@RequestBody SendRequest request, Authentication authentication) {
        requireAdmin(authentication);
        String role = request.recipientRole().toUpperCase();
        if (!role.equals("USER") && !role.equals("SELLER"))
            throw new IllegalArgumentException("Recipient must be a buyer or seller");
        events.publish(new NotificationEvent(authentication.getName(), request.recipientUsername(),
            role, request.message(), Instant.now()));
    }

    @PostMapping("/broadcast") @ResponseStatus(HttpStatus.ACCEPTED)
    public void broadcast(@RequestBody BroadcastRequest request, Authentication authentication) {
        requireAdmin(authentication);
        String role = request.audience().toUpperCase();
        if (!role.equals("USER") && !role.equals("SELLER") && !role.equals("ALL"))
            throw new IllegalArgumentException("Audience must be USER, SELLER, or ALL");
        events.publish(new NotificationEvent(authentication.getName(), "*", role,
            request.message(), Instant.now()));
    }

    @GetMapping("/{username}")
    public List<NotificationResponse> inbox(@PathVariable String username, Authentication authentication) {
        requireOwnerOrAdmin(username, authentication);
        String role = role(authentication);
        return notifications.findInbox(username, role).stream().map(n -> response(n, username)).toList();
    }

    @PutMapping("/{username}/{id}/read")
    public NotificationResponse read(@PathVariable String username, @PathVariable String id,
                                     Authentication authentication) {
        requireOwnerOrAdmin(username, authentication);
        NotificationMessage message = notifications.findById(id).orElseThrow();
        boolean direct = message.getRecipientUsername().equals(username);
        boolean broadcast = message.getRecipientUsername().equals("*")
            && (message.getRecipientRole().equals("ALL") || message.getRecipientRole().equals(role(authentication)));
        if (!direct && !broadcast) throw new IllegalArgumentException("Notification does not belong to user");
        if (direct) {
            message.markRead();
            notifications.save(message);
        } else if (!receipts.existsByNotificationIdAndUsername(id, username)) {
            receipts.save(new NotificationReceipt(id, username));
        }
        return response(message, username);
    }

    private void requireAdmin(Authentication auth) {
        if (auth == null || auth.getAuthorities().stream()
            .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN")))
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    private void requireOwnerOrAdmin(String username, Authentication auth) {
        if (auth == null || (!auth.getName().equals(username) && auth.getAuthorities().stream()
            .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))))
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    private String role(Authentication auth) {
        return auth.getAuthorities().stream().map(a -> a.getAuthority())
            .filter(a -> a.startsWith("ROLE_")).map(a -> a.substring(5)).findFirst().orElse("USER");
    }

    private NotificationResponse response(NotificationMessage notification, String username) {
        boolean read = notification.isReadMessage()
            || receipts.existsByNotificationIdAndUsername(notification.getId(), username);
        return new NotificationResponse(notification.getId(), notification.getSenderUsername(),
            notification.getRecipientUsername(), notification.getRecipientRole(), notification.getMessage(),
            notification.getCreatedAt(), read);
    }

    public record SendRequest(String recipientUsername, String recipientRole, String message) { }
    public record BroadcastRequest(String audience, String message) { }
    public record NotificationResponse(String id, String senderUsername, String recipientUsername,
                                       String recipientRole, String message, Instant createdAt,
                                       boolean readMessage) { }
}

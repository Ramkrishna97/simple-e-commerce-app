package com.emarketplace.user;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Base64;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/users") @CrossOrigin(origins = "http://localhost:4200")
public class UserController {
    private final UserRepository users;
    public UserController(UserRepository users) { this.users=users; }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public MarketplaceUser create(@RequestBody CreateUserRequest request) { return users.save(new MarketplaceUser(request.name(), request.username(), request.email(), request.mobileNumber())); }
    @GetMapping public List<MarketplaceUser> all(Authentication auth){ requireAdmin(auth); return users.findAll(); }
    @GetMapping("/admin/count") public CountResponse count(Authentication auth){requireAdmin(auth);return new CountResponse(users.count());}
    @GetMapping("/{username}") public MarketplaceUser get(@PathVariable String username,Authentication auth) { requireOwnerOrAdmin(username,auth); return find(username); }
    @PutMapping("/{username}") public MarketplaceUser updateProfile(@PathVariable String username, @RequestBody ProfileRequest request,Authentication auth) { requireOwnerOrAdmin(username,auth); MarketplaceUser user=find(username); user.updateProfile(request.name(), request.email(), request.mobileNumber()); return users.save(user); }
    @PutMapping("/{username}/profile-image") public MarketplaceUser updateImage(@PathVariable String username, @RequestBody ImageRequest request,Authentication auth) { requireOwnerOrAdmin(username,auth); MarketplaceUser user=find(username); user.setProfileImageUrl(request.imageUrl()); return users.save(user); }
    @PostMapping(value="/{username}/profile-image/upload",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) public MarketplaceUser uploadImage(@PathVariable String username,@RequestParam("file") MultipartFile file,Authentication auth) throws IOException {requireOwnerOrAdmin(username,auth);if(file.isEmpty())throw new IllegalArgumentException("Choose an image to upload");if(file.getSize()>5*1024*1024)throw new IllegalArgumentException("Profile image must be 5 MB or smaller");String contentType=file.getContentType();if(contentType==null||!contentType.startsWith("image/"))throw new IllegalArgumentException("Only image files are supported");MarketplaceUser user=find(username);user.setProfileImageUrl("data:"+contentType+";base64,"+Base64.getEncoder().encodeToString(file.getBytes()));return users.save(user);}
    @PutMapping("/{username}/addresses") public MarketplaceUser updateAddresses(@PathVariable String username, @RequestBody List<AddressRequest> request,Authentication auth) { requireOwnerOrAdmin(username,auth); MarketplaceUser user=find(username); user.setAddresses(request.stream().map(a -> new MarketplaceUser.Address(a.line1(),a.line2(),a.city(),a.state(),a.postalCode(),a.country())).toList()); return users.save(user); }
    @PostMapping("/{username}/orders/{productId}") public MarketplaceUser addOrderedProduct(@PathVariable String username, @PathVariable String productId,Authentication auth) { requireOwnerOrAdmin(username,auth); MarketplaceUser user=find(username); user.addOrderedProduct(productId); return users.save(user); }
    private void requireOwnerOrAdmin(String username,Authentication auth){if(auth==null||(!username.equals(auth.getName())&&!isAdmin(auth)))throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);}
    private void requireAdmin(Authentication auth){if(auth==null||!isAdmin(auth))throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);}
    private boolean isAdmin(Authentication auth){return auth.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_ADMIN"));}
    private MarketplaceUser find(String username) { return users.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username)); }
    public record CreateUserRequest(String name,String username,String email,String mobileNumber) {} public record ProfileRequest(String name,String email,String mobileNumber) {} public record ImageRequest(String imageUrl) {} public record AddressRequest(String line1,String line2,String city,String state,String postalCode,String country) {} public record CountResponse(long count){}
    @ResponseStatus(HttpStatus.NOT_FOUND) static class UserNotFoundException extends RuntimeException { UserNotFoundException(String username) { super("User not found: "+username); } }
    @ExceptionHandler(IllegalArgumentException.class) @ResponseStatus(HttpStatus.BAD_REQUEST) String invalidUpload(IllegalArgumentException exception){return exception.getMessage();}
}

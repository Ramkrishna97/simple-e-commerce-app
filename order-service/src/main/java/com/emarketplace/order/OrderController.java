package com.emarketplace.order;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/orders") @CrossOrigin(origins = {"http://localhost:4200", "https://simple-marketplace-e-com.netlify.app"}) public class OrderController {
 private final CheckoutService checkout; public OrderController(CheckoutService checkout){this.checkout=checkout;}
 @PostMapping("/{username}/checkout") public PurchaseOrder checkout(@PathVariable String username,@RequestHeader("Authorization")String authorization,Authentication authentication){requireOwner(username,authentication);return checkout.checkout(username,authorization);}
 @GetMapping("/{username}") public List<PurchaseOrder> history(@PathVariable String username,Authentication authentication){requireOwner(username,authentication);return checkout.history(username);}
 @GetMapping("/admin/all") public List<PurchaseOrder> all(Authentication authentication){requireAdmin(authentication);return checkout.allOrders();}
 private void requireOwner(String username,Authentication authentication){if(authentication==null||!username.equals(authentication.getName()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Orders belong to another user");}
 private void requireAdmin(Authentication authentication){if(authentication==null||authentication.getAuthorities().stream().noneMatch(a->a.getAuthority().equals("ROLE_ADMIN")))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Administrator access required");}
}

package com.emarketplace.cart;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/carts") @CrossOrigin(origins = "http://localhost:4200") public class CartController {
 private final CartRepository carts; public CartController(CartRepository carts){this.carts=carts;}
 @GetMapping("/{username}") public Cart cart(@PathVariable String username,Authentication authentication){requireOwner(username,authentication);return findOrCreate(username);}
 @PostMapping("/{username}/items") public Cart add(@PathVariable String username,@RequestBody ItemRequest request,Authentication authentication){requireOwner(username,authentication);Cart cart=findOrCreate(username);cart.add(request.productId(),Math.max(1,request.quantity()));return carts.save(cart);}
 @DeleteMapping("/{username}/items/{productId}") public Cart remove(@PathVariable String username,@PathVariable String productId,Authentication authentication){requireOwner(username,authentication);Cart cart=findOrCreate(username);cart.remove(productId);return carts.save(cart);}
 @DeleteMapping("/{username}/items") public Cart clear(@PathVariable String username,Authentication authentication){requireOwner(username,authentication);Cart cart=findOrCreate(username);cart.clear();return carts.save(cart);}
 @GetMapping("/guest/{guestId}") public Cart guestCart(@PathVariable String guestId){return findOrCreate(guestKey(guestId));}
 @PostMapping("/guest/{guestId}/items") public Cart addGuestItem(@PathVariable String guestId,@RequestBody ItemRequest request){Cart cart=findOrCreate(guestKey(guestId));cart.add(request.productId(),Math.max(1,request.quantity()));return carts.save(cart);}
 @DeleteMapping("/guest/{guestId}/items/{productId}") public Cart removeGuestItem(@PathVariable String guestId,@PathVariable String productId){Cart cart=findOrCreate(guestKey(guestId));cart.remove(productId);return carts.save(cart);}
 @PostMapping("/{username}/merge/{guestId}") public Cart mergeGuestCart(@PathVariable String username,@PathVariable String guestId,Authentication authentication){requireOwner(username,authentication);Cart userCart=findOrCreate(username);carts.findByUsername(guestKey(guestId)).ifPresent(guest->{guest.getItems().forEach(item->userCart.add(item.getProductId(),item.getQuantity()));carts.delete(guest);});return carts.save(userCart);}
 private Cart findOrCreate(String username){return carts.findByUsername(username).orElseGet(()->carts.save(new Cart(username)));}
 private String guestKey(String guestId){if(!guestId.matches("[0-9a-fA-F-]{36}"))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid guest cart id");return "guest:"+guestId;}
 private void requireOwner(String username,Authentication authentication){boolean admin=authentication!=null&&authentication.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_ADMIN"));if(authentication==null||(!authentication.getName().equals(username)&&!admin))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Cart belongs to another user");}
 public record ItemRequest(String productId,int quantity){}
}

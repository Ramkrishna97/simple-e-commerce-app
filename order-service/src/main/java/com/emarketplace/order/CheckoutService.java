package com.emarketplace.order;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service public class CheckoutService {
 private final OrderRepository orders; private final RestClient client=RestClient.create(); private final String cartUrl; private final String productUrl; private final String internalKey;
 public CheckoutService(OrderRepository orders,@Value("${app.cart-service-url}")String cartUrl,@Value("${app.product-service-url}")String productUrl,@Value("${app.internal-api-key}")String internalKey){this.orders=orders;this.cartUrl=cartUrl;this.productUrl=productUrl;this.internalKey=internalKey;}
 @Transactional public PurchaseOrder checkout(String username,String authorization){CartResponse cart=client.get().uri(cartUrl+"/api/carts/"+username).header(HttpHeaders.AUTHORIZATION,authorization).retrieve().body(CartResponse.class);if(cart==null||cart.items()==null||cart.items().isEmpty())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Cart is empty");StockRequest stockRequest=new StockRequest(cart.items().stream().map(item->new StockItem(item.productId(),item.quantity())).toList());ReservedProduct[] reserved=client.post().uri(productUrl+"/api/internal/products/reserve").header("X-Internal-Api-Key",internalKey).body(stockRequest).retrieve().body(ReservedProduct[].class);if(reserved==null)throw new ResponseStatusException(HttpStatus.CONFLICT,"Could not reserve products");PurchaseOrder order=null;try{List<PurchaseOrder.OrderItem> items=Arrays.stream(reserved).map(item->new PurchaseOrder.OrderItem(item.productId(),item.name(),item.quantity(),item.unitPrice())).toList();order=orders.saveAndFlush(new PurchaseOrder(username,items));client.delete().uri(cartUrl+"/api/carts/"+username+"/items").header(HttpHeaders.AUTHORIZATION,authorization).retrieve().toBodilessEntity();return order;}catch(RuntimeException failure){client.post().uri(productUrl+"/api/internal/products/release").header("X-Internal-Api-Key",internalKey).body(stockRequest).retrieve().toBodilessEntity();if(order!=null)orders.delete(order);throw failure;}}
 public List<PurchaseOrder> history(String username){return orders.findByUsernameOrderByOrderedAtDesc(username);}
 public List<PurchaseOrder> allOrders(){return orders.findAll();}
 record CartItem(String productId,int quantity){} record CartResponse(String username,List<CartItem> items){} record StockItem(String productId,int quantity){} record StockRequest(List<StockItem> items){} record ReservedProduct(String productId,String name,int quantity,BigDecimal unitPrice,int availableQuantity){}
}

package com.emarketplace.order;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
@Entity @Table(name="purchase_orders") public class PurchaseOrder {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private String id;
 @Column(nullable=false) private String username; @Column(nullable=false) private Instant orderedAt; @Column(nullable=false) private String status; @Column(nullable=false) private BigDecimal totalAmount;
 @ElementCollection @CollectionTable(name="order_items",joinColumns=@JoinColumn(name="order_id")) private List<OrderItem> items=new ArrayList<>();
 protected PurchaseOrder(){} public PurchaseOrder(String username,List<OrderItem> items){this.username=username;this.items=new ArrayList<>(items);this.orderedAt=Instant.now();this.status="PLACED";this.totalAmount=items.stream().map(item->item.unitPrice.multiply(BigDecimal.valueOf(item.quantity))).reduce(BigDecimal.ZERO,BigDecimal::add);}
 public String getId(){return id;} public String getUsername(){return username;} public Instant getOrderedAt(){return orderedAt;} public String getStatus(){return status;} public BigDecimal getTotalAmount(){return totalAmount;} public List<OrderItem> getItems(){return items;}
 @Embeddable public static class OrderItem { private String productId; private String productName; private int quantity; private BigDecimal unitPrice; protected OrderItem(){} public OrderItem(String productId,String productName,int quantity,BigDecimal unitPrice){this.productId=productId;this.productName=productName;this.quantity=quantity;this.unitPrice=unitPrice;} public String getProductId(){return productId;} public String getProductName(){return productName;} public int getQuantity(){return quantity;} public BigDecimal getUnitPrice(){return unitPrice;} }
}

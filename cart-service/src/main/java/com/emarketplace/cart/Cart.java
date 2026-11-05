package com.emarketplace.cart;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
@Entity @Table(name="carts") public class Cart {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private String id;
    @Column(unique=true,nullable=false) private String username;
    @ElementCollection private List<CartItem> items = new ArrayList<>();
    protected Cart() {} public Cart(String username) { this.username=username; }
    public String getUsername(){return username;} public List<CartItem> getItems(){return items;}
    public void add(String productId,int quantity){items.stream().filter(i->i.getProductId().equals(productId)).findFirst().ifPresentOrElse(i->i.setQuantity(i.getQuantity()+quantity),()->items.add(new CartItem(productId,quantity)));}
    public void remove(String productId){items.removeIf(i->i.getProductId().equals(productId));}
    public void clear(){items.clear();}
    @Embeddable public static class CartItem { private String productId; private int quantity; protected CartItem(){} CartItem(String productId,int quantity){this.productId=productId;this.quantity=quantity;} public String getProductId(){return productId;} public int getQuantity(){return quantity;} void setQuantity(int quantity){this.quantity=quantity;} }
}

package com.emarketplace.product;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Entity public class Product {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    private String name; @Column(length=4000) private String description; private String category; private BigDecimal discount; private BigDecimal mrpPrice; private BigDecimal discountedPrice; private String sellerUsername; private Integer availableQuantity;
    @ElementCollection private List<String> reviews = new ArrayList<>(); @ElementCollection private List<String> images = new ArrayList<>();
    protected Product() {} public Product(ProductRequest r, String sellerUsername) { apply(r); this.sellerUsername=sellerUsername; }
    public void apply(ProductRequest r) { name=r.name();description=r.description();category=r.category();reviews=new ArrayList<>(r.reviews() == null ? List.of() : r.reviews());images=new ArrayList<>(r.images() == null ? List.of() : r.images());discount=r.discount();mrpPrice=r.mrpPrice();discountedPrice=r.discountedPrice();if(r.availableQuantity()!=null)availableQuantity=Math.max(0,r.availableQuantity()); }
    public void reserve(int quantity){if(quantity<=0||getAvailableQuantity()<quantity)throw new IllegalArgumentException("Insufficient stock for "+name);availableQuantity=getAvailableQuantity()-quantity;} public void release(int quantity){availableQuantity=getAvailableQuantity()+Math.max(0,quantity);} public void initializeStock(int quantity){if(availableQuantity==null||availableQuantity<=0)availableQuantity=quantity;}
    public String getId(){return id;} public String getName(){return name;} public String getDescription(){return description;} public String getCategory(){return category;} public List<String> getReviews(){return reviews;} public List<String> getImages(){return images;} public BigDecimal getDiscount(){return discount;} public BigDecimal getMrpPrice(){return mrpPrice;} public BigDecimal getDiscountedPrice(){return discountedPrice;} public String getSellerUsername(){return sellerUsername;} public int getAvailableQuantity(){return availableQuantity==null?0:availableQuantity;}
}

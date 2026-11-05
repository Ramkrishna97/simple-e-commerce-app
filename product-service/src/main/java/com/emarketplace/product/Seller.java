package com.emarketplace.product;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
@Entity public class Seller {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    private String name; @Column(unique=true, nullable=false) private String username; @Column(unique=true, nullable=false) private String email; private String mobileNumber;
    @ElementCollection private List<String> listedProductIds = new ArrayList<>();
    protected Seller() {} public Seller(String name,String username,String email,String mobileNumber) { this.name=name;this.username=username;this.email=email;this.mobileNumber=mobileNumber; }
    public String getId(){return id;} public String getName(){return name;} public String getUsername(){return username;} public String getEmail(){return email;} public String getMobileNumber(){return mobileNumber;} public List<String> getListedProductIds(){return listedProductIds;} public void addProduct(String productId) { listedProductIds.add(productId); }
    public void update(String name,String email,String mobileNumber){this.name=name;this.email=email;this.mobileNumber=mobileNumber;}
}

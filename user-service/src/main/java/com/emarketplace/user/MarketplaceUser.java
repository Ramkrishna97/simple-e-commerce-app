package com.emarketplace.user;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "users")
public class MarketplaceUser {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    private String name;
    @Column(unique = true, nullable = false) private String username;
    @Column(unique = true, nullable = false) private String email;
    private String mobileNumber; @Lob @Column(columnDefinition="CLOB") private String profileImageUrl;
    @ElementCollection private List<String> orderedProductIds = new ArrayList<>();
    @ElementCollection @CollectionTable(name = "user_addresses", joinColumns = @JoinColumn(name = "user_id")) private List<Address> addresses = new ArrayList<>();
    protected MarketplaceUser() { }
    public MarketplaceUser(String name, String username, String email, String mobileNumber) { this.name=name; this.username=username; this.email=email; this.mobileNumber=mobileNumber; }
    public String getId() { return id; } public String getName() { return name; } public String getUsername() { return username; } public String getEmail() { return email; } public String getMobileNumber() { return mobileNumber; } public String getProfileImageUrl() { return profileImageUrl; } public List<Address> getAddresses() { return addresses; } public List<String> getOrderedProductIds() { return orderedProductIds; }
    public void updateProfile(String name, String email, String mobileNumber) { this.name=name; this.email=email; this.mobileNumber=mobileNumber; } public void setProfileImageUrl(String url) { profileImageUrl=url; } public void setAddresses(List<Address> values) { addresses = new ArrayList<>(values); } public void addOrderedProduct(String productId) { orderedProductIds.add(productId); }
    @Embeddable public static class Address { private String line1; private String line2; private String city; private String state; private String postalCode; private String country; protected Address() {} public Address(String line1,String line2,String city,String state,String postalCode,String country) {this.line1=line1;this.line2=line2;this.city=city;this.state=state;this.postalCode=postalCode;this.country=country;} public String getLine1(){return line1;} public String getLine2(){return line2;} public String getCity(){return city;} public String getState(){return state;} public String getPostalCode(){return postalCode;} public String getCountry(){return country;} }
}

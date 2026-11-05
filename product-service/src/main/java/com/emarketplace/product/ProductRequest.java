package com.emarketplace.product;
import java.math.BigDecimal;
import java.util.List;
public record ProductRequest(String name,String description,String category,List<String> reviews,List<String> images,BigDecimal discount,BigDecimal mrpPrice,BigDecimal discountedPrice,Integer availableQuantity) { }

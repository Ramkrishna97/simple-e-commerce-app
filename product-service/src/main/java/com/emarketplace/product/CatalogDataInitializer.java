package com.emarketplace.product;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CatalogDataInitializer {
    @Bean
    CommandLineRunner initializeCatalog(ProductRepository products, SellerRepository sellers) {
        return args -> {
            if (products.count() > 0) { List<Product> existing=products.findAll();existing.forEach(product -> product.initializeStock(25));products.saveAllAndFlush(existing);return; }
            Seller seller = sellers.findByUsername("marketlane")
                .orElseGet(() -> sellers.save(new Seller("Market Lane", "marketlane", "seller@marketlane.local", "9999999999")));
            List<ProductRequest> samples = List.of(
                product("Everyday Wireless Headphones", "Comfortable over-ear headphones with clear sound and long battery life.", "Electronics", "2499", "1799", "28"),
                product("Minimal Desk Lamp", "A warm, adjustable desk light for focused work and relaxed evenings.", "Home", "1899", "1299", "32"),
                product("Classic Canvas Backpack", "A durable everyday backpack with a padded laptop compartment.", "Fashion", "2199", "1499", "32"),
                product("Stainless Steel Bottle", "Double-wall insulated bottle that keeps drinks cold throughout the day.", "Lifestyle", "999", "699", "30"),
                product("Smart Fitness Band", "Track steps, sleep, heart rate, and everyday activity from your wrist.", "Electronics", "3499", "2499", "29"),
                product("Organic Cotton Throw", "A soft textured throw made for sofas, reading corners, and cool nights.", "Home", "1599", "1099", "31"),
                product("Everyday Running Shoes", "Lightweight trainers with cushioned support for walks and daily runs.", "Fashion", "2999", "2199", "27"),
                product("Ceramic Coffee Set", "Four modern stoneware mugs with a calm matte finish.", "Kitchen", "1299", "899", "31")
            );
            for (ProductRequest request : samples) {
                Product saved = products.save(new Product(request, seller.getUsername()));
                seller.addProduct(saved.getId());
            }
            sellers.save(seller);
        };
    }

    private ProductRequest product(String name, String description, String category, String mrp, String price, String discount) {
        return new ProductRequest(name, description, category, List.of(), List.of(), new BigDecimal(discount), new BigDecimal(mrp), new BigDecimal(price), 25);
    }
}

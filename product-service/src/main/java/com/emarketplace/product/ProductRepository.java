package com.emarketplace.product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
interface ProductRepository extends JpaRepository<Product,String> { List<Product> findBySellerUsername(String username); }
interface SellerRepository extends JpaRepository<Seller,String> { Optional<Seller> findByUsername(String username); }

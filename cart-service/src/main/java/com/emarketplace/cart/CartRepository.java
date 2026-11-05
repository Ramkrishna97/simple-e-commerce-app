package com.emarketplace.cart;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
interface CartRepository extends JpaRepository<Cart,String> { Optional<Cart> findByUsername(String username); }

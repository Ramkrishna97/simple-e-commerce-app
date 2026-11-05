package com.emarketplace.user;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UserRepository extends JpaRepository<MarketplaceUser, String> { Optional<MarketplaceUser> findByUsername(String username); }

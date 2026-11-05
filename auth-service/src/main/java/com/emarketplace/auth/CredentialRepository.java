package com.emarketplace.auth;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CredentialRepository extends JpaRepository<Credential, String> {
    Optional<Credential> findByUsername(String username);
    Optional<Credential> findByEmail(String email);
}

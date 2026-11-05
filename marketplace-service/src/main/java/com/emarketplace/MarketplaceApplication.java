package com.emarketplace;

import com.emarketplace.auth.Credential;
import com.emarketplace.auth.CredentialRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class MarketplaceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MarketplaceApplication.class, args);
    }

    @Bean
    CommandLineRunner provisionAdmin(CredentialRepository credentials, PasswordEncoder encoder) {
        return args -> {
            if (credentials.findByUsername("admin").isEmpty()) {
                credentials.save(new Credential("admin", "admin@marketplace.local",
                    encoder.encode("qwertyuiop"), "ADMIN"));
            }
        };
    }
}

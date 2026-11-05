package com.emarketplace.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class AuthServiceApplication {
    public static void main(String[] args) { SpringApplication.run(AuthServiceApplication.class, args); }

    @Bean
    CommandLineRunner provisionAdmin(CredentialRepository credentials, PasswordEncoder encoder) {
        return args -> {
            if (credentials.findByUsername("admin").isEmpty()) {
                credentials.save(new Credential("admin", "admin@marketplace.local", encoder.encode("qwertyuiop"), "ADMIN"));
            }
        };
    }
}

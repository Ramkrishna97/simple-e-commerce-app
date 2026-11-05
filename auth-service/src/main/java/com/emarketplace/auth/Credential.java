package com.emarketplace.auth;

import jakarta.persistence.*;

@Entity
@Table(name = "credentials")
public class Credential {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @Column(unique = true, nullable = false) private String username;
    @Column(unique = true, nullable = false) private String email;
    @Column(nullable = false) private String passwordHash;
    @Column(nullable = false) private String role;
    private boolean temporaryPassword;
    protected Credential() { }
    public Credential(String username, String email, String passwordHash, String role) { this.username = username; this.email = email; this.passwordHash = passwordHash; this.role = role; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
    public boolean isTemporaryPassword() { return temporaryPassword; }
    public void setPasswordHash(String passwordHash, boolean temporaryPassword) { this.passwordHash = passwordHash; this.temporaryPassword = temporaryPassword; }
}

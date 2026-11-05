package com.emarketplace.auth;

import java.security.SecureRandom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private final CredentialRepository credentials; private final PasswordEncoder encoder; private final JwtService jwtService; private final int tempLength;
    public AuthService(CredentialRepository credentials, PasswordEncoder encoder, JwtService jwtService, @Value("${app.temporary-password-length}") int tempLength) { this.credentials = credentials; this.encoder = encoder; this.jwtService = jwtService; this.tempLength = tempLength; }
    public Credential register(RegisterRequest request) {
        String role = request.role() == null ? "USER" : request.role().trim().toUpperCase();
        if (!role.equals("USER") && !role.equals("SELLER")) throw new IllegalArgumentException("Only buyer and seller accounts can be created");
        if ("admin".equalsIgnoreCase(request.username())) throw new IllegalArgumentException("That username is reserved");
        if (credentials.findByUsername(request.username()).isPresent() || credentials.findByEmail(request.email()).isPresent()) throw new IllegalArgumentException("Username or email already exists");
        return credentials.save(new Credential(request.username(), request.email(), encoder.encode(request.password()), role));
    }
    public LoginResponse login(LoginRequest request) {
        Credential credential = credentials.findByUsername(request.username()).orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        if (!encoder.matches(request.password(), credential.getPasswordHash())) throw new IllegalArgumentException("Invalid username or password");
        return new LoginResponse(jwtService.createToken(credential), credential.getRole(), credential.isTemporaryPassword());
    }
    @Transactional public void changePassword(String username, ChangePasswordRequest request) {
        Credential credential = credentials.findByUsername(username).orElseThrow();
        if (!encoder.matches(request.currentPassword(), credential.getPasswordHash())) throw new IllegalArgumentException("Current password is incorrect");
        credential.setPasswordHash(encoder.encode(request.newPassword()), false);
    }
    @Transactional public TemporaryPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        Credential credential = credentials.findByEmail(request.email()).orElseThrow(() -> new IllegalArgumentException("No account matches that email"));
        String temporary = temporaryPassword(); credential.setPasswordHash(encoder.encode(temporary), true);
        return new TemporaryPasswordResponse(temporary, "Deliver this value by email in production; it is returned here only for local development.");
    }
    private String temporaryPassword() { SecureRandom random = new SecureRandom(); StringBuilder value = new StringBuilder(); for (int i=0; i<tempLength; i++) value.append(ALPHABET.charAt(random.nextInt(ALPHABET.length()))); return value.toString(); }
    public record RegisterRequest(String username, String email, String password, String role) { }
    public record LoginRequest(String username, String password) { }
    public record ChangePasswordRequest(String currentPassword, String newPassword) { }
    public record ForgotPasswordRequest(String email) { }
    public record LoginResponse(String accessToken, String role, boolean passwordChangeRequired) { }
    public record TemporaryPasswordResponse(String temporaryPassword, String note) { }
}

package com.emarketplace.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@CrossOrigin(origins = {"http://localhost:4200", "https://simple-marketplace-e-com.netlify.app"})
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }
    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED) public void register(@RequestBody AuthService.RegisterRequest request) { authService.register(request); }
    @PostMapping("/login") public AuthService.LoginResponse login(@RequestBody AuthService.LoginRequest request) { return authService.login(request); }
    @PostMapping("/forgot-password") public AuthService.TemporaryPasswordResponse forgotPassword(@RequestBody AuthService.ForgotPasswordRequest request) { return authService.forgotPassword(request); }
    @PutMapping("/{username}/password") @ResponseStatus(HttpStatus.NO_CONTENT) public void changePassword(@PathVariable String username, @RequestBody AuthService.ChangePasswordRequest request,Authentication authentication) { if(authentication==null||!username.equals(authentication.getName()))throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN); authService.changePassword(username, request); }
    @ExceptionHandler(IllegalArgumentException.class) @ResponseStatus(HttpStatus.BAD_REQUEST) public String badRequest(IllegalArgumentException exception) { return exception.getMessage(); }
}

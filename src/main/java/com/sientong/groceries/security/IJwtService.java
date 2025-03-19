package com.sientong.groceries.security;

public interface IJwtService {
    String generateToken(String username, String role);
    String validateTokenAndGetUsername(String token);
}

package org.example.usersvc.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.example.usersvc.infrastructure.properties.JwtProperties;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final SecretKey key;
    private final JwtProperties properties;

    public String generateAccessToken(UserPrincipal user) {
        List<String> roles = user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        Map<String, Object> claims = Map.of(
                "userId", user.id().toString(),
                "roles", roles
        );

        return Jwts.builder()
                .subject(user.getUsername())
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + properties.expirationAccess()))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(UserPrincipal user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.id().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + properties.expirationRefresh()))
                .signWith(key)
                .compact();
    }

    public boolean isValid(String token, UserPrincipal user) {
        String username = extractUsername(token);
        UUID userId = extractUserId(token);
        Date expiration = extractClaims(token).getExpiration();

        return Objects.equals(username, user.getUsername())
                && Objects.equals(userId, user.id())
                && expiration.after(new Date());
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public UUID extractUserId(String token) {
        Object rawUserId = extractClaims(token).get("userId");

        if (rawUserId == null) {
            return null;
        }

        return UUID.fromString(rawUserId.toString());
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

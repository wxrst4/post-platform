package org.example.socialsvc.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final SecretKey key;

    public UserPrincipal parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        UUID userId = UUID.fromString(claims.get("userId").toString());
        String username = claims.getSubject();

        List<?> rawRoles = claims.get("roles", List.class);

        List<SimpleGrantedAuthority> authorities = rawRoles == null
                ? Collections.emptyList()
                : rawRoles.stream()
                .map(Object::toString)
                .map(SimpleGrantedAuthority::new)
                .toList();


        return new UserPrincipal(userId, username, authorities);
    }
}

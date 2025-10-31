package com.biobac.warehouse.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;

@Component
public class JwtUtil {
    private final Key key;

    public JwtUtil(@Value("${jwt.secret}") String jwtSecret) {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String extractUsername(String token) {
        return parseClaims(token).getBody().getSubject();
    }

    public Long extractUserId(String token) {
        Object id = parseClaims(token).getBody().get("userId");
        return id == null ? null :
                id instanceof Number ? ((Number) id).longValue() :
                        Long.parseLong(id.toString());
    }

    public List<String> extractRoles(String token) {
        Claims claims = parseClaims(token).getBody();
        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof Collection<?> coll) {
            List<String> roles = new ArrayList<>();
            for (Object o : coll) {
                if (o != null) roles.add(o.toString());
            }
            return roles;
        }
        return Collections.emptyList();
    }

    public List<String> extractPermissions(String token) {
        Claims claims = parseClaims(token).getBody();
        Object permsObj = claims.get("perms");
        if (permsObj instanceof Collection<?> coll) {
            List<String> perms = new ArrayList<>();
            for (Object o : coll) {
                if (o != null) perms.add(o.toString());
            }
            return perms;
        }
        return Collections.emptyList();
    }

    public void validateAccessToken(String token) throws JwtException, IllegalArgumentException {
        parseClaims(token);
    }

    private Jws<Claims> parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }
}

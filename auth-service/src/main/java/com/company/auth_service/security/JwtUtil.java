package com.company.auth_service.security;

import com.company.auth_service.entity.User;
import com.company.auth_service.entity.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expiry-minutes}")
    private Long accessExpiryMinutes;


    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }


    /* Generate Access Token */
    public String generateAccessToken(User user) {

        List<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        Instant now = Instant.now();

        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(accessExpiryMinutes, ChronoUnit.MINUTES)))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    /* Extract Claims */
    private Claims getClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    /* Validate Token */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }


    /* Get UserId */
    public UUID getUserId(String token) {
        return UUID.fromString(getClaims(token).getSubject());
    }


    /* Get Roles */
    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {

        return (List<String>) getClaims(token).get("roles");
    }


    /* Get Email */
    public String getEmail(String token) {
        return getClaims(token).get("email", String.class);
    }
}


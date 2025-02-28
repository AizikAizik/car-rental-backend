package com.example.demo.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expiration}")
  private long expirationTime;

  private SecretKey getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secret);
    return Keys.hmacShaKeyFor(keyBytes);
  }


  public String generateToken(String email) {
    return Jwts.builder()
            .subject(email).
            issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expirationTime))
            .signWith(SignatureAlgorithm.HS256, secret)
            .compact();
  }

  public String extractEmail(String token) {
    return Jwts.parser()
            .setSigningKey(getSigningKey()) // Use parserBuilder() instead
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
  }

  public boolean validateToken(String token) {
    try {
      byte[] bytes = Decoders.BASE64.decode(secret);
      SecretKey key = Keys.hmacShaKeyFor(bytes);
      Jwts.parser().verifyWith(key).build().parseClaimsJws(token);
      return true;
    } catch (JwtException e) {
      return false;
    }
  }
}


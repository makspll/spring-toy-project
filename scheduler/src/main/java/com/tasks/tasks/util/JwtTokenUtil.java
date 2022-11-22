package com.tasks.tasks.util;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tasks.tasks.entities.Account;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtTokenUtil {
    private static final long EXPIRE_DURATION = 10 * 60 * 10 * 1000; // 10 minutes

    @Value("${application.jwt.secret}")
    private String SECRET_KEY;

    public String generateAccessToken(Account user) {
        return Jwts.builder()
                .setSubject(user.getUsername().toString())
                .setIssuer("Tasks")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_DURATION))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();

    }

    public boolean validateAccessToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    public String getSubject(String token) {
        return parseClaims(token).getSubject();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }
}

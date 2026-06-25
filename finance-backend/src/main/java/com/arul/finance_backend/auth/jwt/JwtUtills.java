package com.arul.finance_backend.auth.jwt;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.arul.finance_backend.ledger.enums.UserRole;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtills {
    
    @Value("${spring.jwt.SECRET_KEY}")
    String secretKey;

    @Value("${spring.jwt.EXPIRATION_TIME}")
    private long expirationTime;
    

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public String extractUserName(String token){
        return getClaims(token).getSubject();
    }

    public Boolean isTokenValid(String token){
        try{

            return getClaims(token).getExpiration().after(new Date());

        }catch(JwtException e){
            return false;
        }
    }

    public SecretKey getSignInKey(){
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String getToken(String userName, UserRole role){
        if (userName == null || userName.isBlank()){
            System.out.println("username is null #######################");
            throw new IllegalArgumentException("Cannot mint JWT: username is null/blank");
        }
        return Jwts.builder()
                .subject(userName)
                .claim("Role", role.name() )
                .issuedAt(new Date())
                .signWith(getSignInKey())
                .expiration(new Date( System.currentTimeMillis()+ expirationTime))
                .compact();
    }

}

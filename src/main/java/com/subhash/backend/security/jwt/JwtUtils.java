package com.subhash.backend.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtils {

    private String jwtSecret;

    public JwtUtils(){
        try {
            KeyGenerator keyGenerator=KeyGenerator.getInstance("HmacSHA256");
            SecretKey secretKey=keyGenerator.generateKey();
            jwtSecret= Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpiration;

    public String generateToken(UserDetails userDetails) {
        String userName=userDetails.getUsername();
        return Jwts.builder()
                .subject(userName)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime()+ jwtExpiration))
                .signWith(getKey())
                .compact();
    }

    private Key getKey() {
        byte [] keyBytes= Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken=request.getHeader("Authorization");
        if(bearerToken != null && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        else{
            return null;
        }
    }

    public boolean validateToken(String jwt) {
        try {
            Jwts.parser()
                    .verifyWith((SecretKey) getKey())
                    .build()
                    .parseSignedClaims(jwt);
            return true;
        }catch (ExpiredJwtException e){
            System.out.println(e.getMessage());
        }
        return false;
    }

    public String getUserNamefromJwtToken(String jwt) {
        return Jwts.parser()
                .verifyWith((SecretKey) getKey())
                .build()
                .parseSignedClaims(jwt)
                .getPayload().getSubject();
    }
}


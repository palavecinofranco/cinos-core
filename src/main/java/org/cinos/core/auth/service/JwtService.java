package org.cinos.core.auth.service;

import org.cinos.core.users.dto.UserDTO;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String SECRET_KEY;
    @Value("${security.jwt.expiration-time-minutes}")
    private Integer EXPIRATION_TIME_MINUTES;
    @Value("${security.jwt.refresh-expiration-time-days}")
    private Integer REFRESH_EXPIRATION_TIME_DAYS;

    public String generateToken(UserDTO userDTO) {
        return Jwts.builder()
                .subject(userDTO.username())
                .claim("name", userDTO.name())
                .claim("lastname", userDTO.lastname())
                .claim("email", userDTO.email())
                .claim("roles", userDTO.roles().stream().map(Enum::name).toList())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MINUTES * 60 * 1000))
                .header()
                .type("JWT")
                .and()
                .signWith(generateKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(UserDTO userDTO) {
        return Jwts.builder()
                .subject(userDTO.username())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(Date.from(LocalDateTime.now().plusDays(REFRESH_EXPIRATION_TIME_DAYS).atZone(ZoneId.systemDefault()).toInstant()))
                .header()
                .type("JWT")
                .and()
                .signWith(generateKey(), Jwts.SIG.HS256)
                .compact();
    }

    private SecretKey generateKey(){
        byte[] passwordDecode = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(passwordDecode);
    }

    public String extractUsername(final String token) {
        return extractPayload(token).getSubject();
    }

    private Claims extractPayload(final String token) {
        return Jwts.parser().verifyWith(generateKey()).build().parseSignedClaims(token).getPayload();
    }

    public boolean isValidRefreshToken(String token) {

        try {
            extractUsername(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}

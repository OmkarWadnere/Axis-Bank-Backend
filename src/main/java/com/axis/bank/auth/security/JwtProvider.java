package com.axis.bank.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.axis.bank.utility.Constants.ROLES;

@Component
public class JwtProvider {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final long accessTokenValidityMs;
    private final long refreshTokenValidityMs;

    public JwtProvider(RsaKeyLoader rsaKeyLoader,
                       @Value("${app.jwt.private-key-path}") String privateKeyPath,
                       @Value("${app.jwt.public-key-path}") String publicKeyPath,
                       @Value("${app.jwt.access-token-validity-seconds}") long accessSec,
                       @Value("${app.jwt.refresh-token-validity-seconds}") long refreshSec) throws Exception {
        this.privateKey = rsaKeyLoader.loadPrivateKey(privateKeyPath);
        this.publicKey = rsaKeyLoader.loadPublicKey(publicKeyPath);
        this.accessTokenValidityMs = accessSec * 1000;
        this.refreshTokenValidityMs = refreshSec * 1000;
    }

    public String generateAccessToken(UserDetails userDetails) {
        long now = System.currentTimeMillis();
        Map<String, Object> claims = new HashMap<>();
        claims.put(ROLES, userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList());
        return Jwts.builder().claims(claims).subject(userDetails.getUsername()).issuedAt(new Date(now)).expiration(new Date(now + accessTokenValidityMs))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public String generateRefreshToken(UserDetails userDetails) {
        long now = System.currentTimeMillis();
        Map<String, Object> claims = new HashMap<>();
        claims.put(ROLES, userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList());
        return Jwts.builder().subject(userDetails.getUsername()).claims(claims).issuedAt(new Date(now)).expiration(new Date(now + refreshTokenValidityMs))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String getSubject(String token) {
        return Jwts.parser().verifyWith(
                publicKey).build().parseSignedClaims(token).getPayload().getSubject();
    }

    public Date getExpiryDate(String token) {
        return Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token).getPayload().getExpiration();
    }

    public long getRemainingValidity(String token) {
        Date expiration = getExpiryDate(token);
        return expiration.getTime() - System.currentTimeMillis();
    }

    // Extract roles
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        Object roles = claims.get(ROLES);

        if (roles instanceof List<?>) {
            return ((List<?>) roles).stream()
                    .map(Object::toString).toList();
        }
        return List.of();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

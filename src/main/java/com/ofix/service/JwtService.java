package com.ofix.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    // Clave secreta utilizada para firmar los tokens.
    private static final String SECRET_KEY =
            "bXlfb2ZpeF9qd3dfc2VjcmV0X2tleV8yMDI2X29maXhfYXBpX3NlY3JldA==";

    // Duración del token: 1 hora.
    private static final long EXPIRATION_TIME =
            1000 * 60 * 60;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Genera un JWT utilizando el email del usuario.
    public String generarToken(String email) {

        Date ahora = new Date();
        Date expiracion =
                new Date(ahora.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .subject(email)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(getSigningKey())
                .compact();
    }

    // Obtiene el email almacenado dentro del JWT.
    public String extraerEmail(String token) {

        return extraerClaim(
                token,
                Claims::getSubject
        );
    }

    public boolean esTokenValido(
            String token,
            String email) {

        String emailDelToken = extraerEmail(token);

        return emailDelToken.equals(email);
    }

    // Obtiene un dato específico del token.
    private <T> T extraerClaim(
            String token,
            Function<Claims, T> resolver) {

        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return resolver.apply(claims);
    }
}
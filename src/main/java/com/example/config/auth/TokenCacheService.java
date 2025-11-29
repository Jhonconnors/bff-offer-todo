package com.example.config.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.function.Supplier;

@Service
@EnableRetry
public class TokenCacheService {

    private String cachedToken;
    private Instant expiration;

    @Autowired
    private PublicKeyCacheService publicKeyCacheService;

    @Retryable(
            retryFor = { SignatureException.class, MalformedJwtException.class, SecurityException.class },
            maxAttempts = 2,
            backoff = @Backoff(delay = 0)
    )
    public synchronized String getValidToken(Supplier<String> tokenSupplier) throws Exception {
        if (cachedToken == null || expiration == null || Instant.now().isAfter(expiration.minusSeconds(20))) {
            // Renovar si: // - No existe token // - No hay fecha expiration // - Esta por expirar en <20 segundos
            String newToken = tokenSupplier.get();

            try {
                // Extraer expiration del JWT emitido por tu Token Server
                RSAPublicKey publicKey = publicKeyCacheService.getPublicKey();
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(publicKey)
                        .build()
                        .parseClaimsJws(newToken)
                        .getBody();
                cachedToken = newToken;
                expiration = claims.getExpiration().toInstant();

            } catch (Exception e) {
                //clave inválida → limpiar cache para que se refresque en el siguiente intento
                publicKeyCacheService.publicKeyClean();
                throw e;
            }
        }
        return cachedToken;
    }


    @Recover
    public String recoverJwtValidation(Exception ex, Supplier<String> tokenSupplier) {
        throw new RuntimeException("No pudo validarse el JWT incluso después del retry", ex);
    }
}

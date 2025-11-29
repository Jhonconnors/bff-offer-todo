package com.example.config.auth;

import com.example.config.auth.model.TokenRequest;
import com.example.config.auth.model.TokenResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
public class TokenClient {

    private final RestTemplate restTemplate;
    private final TokenCacheService tokenCacheService;

    @Value("${app.security.token-server-url}")
    private String tokenServerUrl;

    @Value("${app.security.private-key}")
    private String privateKeyPem;

    @Value("${app.security.client-id}")
    private UUID clientId;

    public TokenClient(@Qualifier("simpleRestTemplate") RestTemplate restTemplate,
                       TokenCacheService cache) {
        this.restTemplate = restTemplate;
        this.tokenCacheService = cache;
    }

    public String obtainAccessToken() throws Exception {

        return tokenCacheService.getValidToken(() -> {
            try {
                return requestNewToken();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private String requestNewToken() throws Exception {

        // --- convertir llave privada ---
        String cleaned = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] keyBytes = Base64.getDecoder().decode(cleaned);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(spec);

        // --- firmar JWT del cliente ---
        String clientJwt = Jwts.builder()
                .setSubject(clientId.toString())
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plusSeconds(120))) // 2 min
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();

        TokenRequest body = new TokenRequest(clientId.toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + clientJwt);

        return restTemplate.exchange(
                tokenServerUrl + "/token",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                TokenResponse.class
        ).getBody().getAccessToken();
    }

}

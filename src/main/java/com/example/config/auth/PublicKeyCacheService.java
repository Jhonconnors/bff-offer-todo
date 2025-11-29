package com.example.config.auth;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class PublicKeyCacheService {

    @Value("${app.security.token-server-url}")
    private String tokenServerUrl;

    private RSAPublicKey cachedKey;
    private Instant lastRefresh;

    private static final Duration REFRESH_INTERVAL = Duration.ofHours(24);

    private final RestTemplate restTemplate;

    public PublicKeyCacheService(@Qualifier("simpleRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void publicKeyClean(){
        cachedKey = null;
    }

    public synchronized RSAPublicKey getPublicKey() throws Exception {

        if (cachedKey == null || lastRefresh == null ||
                Instant.now().isAfter(lastRefresh.plus(REFRESH_INTERVAL))) {

            refreshPublicKey();
        }

        return cachedKey;
    }

    private void refreshPublicKey() throws Exception {
        String url = tokenServerUrl + "/certs";

        Map<String, Object> jwkSet = restTemplate.getForObject(url, Map.class);

        List<Map<String, String>> keys = (List<Map<String, String>>) jwkSet.get("keys");

        Map<String, String> jwk = keys.get(0);

        // Extraer n y e (Base64URL)
        String n = jwk.get("n");
        String e = jwk.get("e");

        cachedKey = convertJwkToRSAPublicKey(n, e);
        lastRefresh = Instant.now();
    }

    private RSAPublicKey convertJwkToRSAPublicKey(String n, String e) throws Exception {

        BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(n));
        BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(e));

        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
        KeyFactory kf = KeyFactory.getInstance("RSA");

        return (RSAPublicKey) kf.generatePublic(spec);
    }
}

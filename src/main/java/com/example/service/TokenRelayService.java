package com.example.service;

import com.example.config.auth.model.TokenResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
public class TokenRelayService {

    private final RestTemplate restTemplate;

    @Value("${app.security.token-server-url}")
    private String tokenServerUrl;

    public TokenRelayService(@Qualifier("simpleRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public TokenResponse requestTokenToTokenServer(String clientJwt) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + clientJwt);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<?> entity = new HttpEntity<>(Collections.emptyMap(), headers);

        ResponseEntity<TokenResponse> resp = restTemplate.exchange(
                tokenServerUrl + "/auth/web/token",
                HttpMethod.POST,
                entity,
                TokenResponse.class
        );

        return resp.getBody();
    }
}

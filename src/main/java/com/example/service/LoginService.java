package com.example.service;

import com.example.model.token.server.LoginRequest;
import com.example.model.token.server.LoginResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LoginService {

    @Value("${app.security.token-server-url}")
    private String tokenServerUrl;
    private final RestTemplate restTemplate;

    public LoginService(@Qualifier("simpleRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public LoginResponse generateLogin(LoginRequest body) {
        HttpHeaders headers = new HttpHeaders();

        return restTemplate.exchange(
                tokenServerUrl +"/auth/login",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                LoginResponse.class).getBody();
    }

}

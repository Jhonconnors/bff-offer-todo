package com.example.config.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TokenInterceptor implements ClientHttpRequestInterceptor {


    private final TokenClient tokenClient;

    public TokenInterceptor(TokenClient tokenClient) {
        this.tokenClient = tokenClient;
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, byte[] body,
            ClientHttpRequestExecution execution) throws IOException {

        try {
            String accessToken = tokenClient.obtainAccessToken();
            request.getHeaders().add("Authorization", "Bearer " + accessToken);
        } catch (Exception e) {
            throw new RuntimeException("Cannot obtain token", e);
        }

        return execution.execute(request, body);
    }
}

package com.example.service;

import com.example.model.supermarket.ProductListSupermarket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class SupermarketService {

    private final RestTemplate restTemplate;

    private final String supermarketBaseUrl;

    public SupermarketService(RestTemplate restTemplate,
                           @Value("${app.supermarket.url}") String supermarketBaseUrl) {
        this.restTemplate = restTemplate;
        this.supermarketBaseUrl = supermarketBaseUrl;
    }

    public ProductListSupermarket getProducts(String query, String comuna) {
        String finalUrl = supermarketBaseUrl +"/v1/supermarkets/product";

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(finalUrl)
                .queryParam("q", query);

        // parámetro opcional
        if (comuna != null && !comuna.isBlank()) {
            builder.queryParam("comuna", comuna);
        }

        String url = builder.toUriString();

        return restTemplate.getForObject(url, ProductListSupermarket.class);
    }
}

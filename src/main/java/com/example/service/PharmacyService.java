package com.example.service;

import com.example.model.farmacy.ProductListPharmacy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class PharmacyService {
    private final RestTemplate restTemplate;

    private final String pharmacyBaseUrl;

    public PharmacyService(RestTemplate restTemplate,
                           @Value("${app.pharmacy.url}") String pharmacyBaseUrl) {
        this.restTemplate = restTemplate;
        this.pharmacyBaseUrl = pharmacyBaseUrl;
    }

    public ProductListPharmacy getProducts(String query, String comuna) {
        String finalUrl = pharmacyBaseUrl+"/v1/farmacy/medicament/product";

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(finalUrl)
                .queryParam("q", query);

        // parámetro opcional
        if (comuna != null && !comuna.isBlank()) {
            builder.queryParam("comuna", comuna);
        }

        String url = builder.toUriString();

        return restTemplate.getForObject(url, ProductListPharmacy.class);
    }
}

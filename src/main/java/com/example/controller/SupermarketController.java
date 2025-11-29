package com.example.controller;


import com.example.model.supermarket.ProductListSupermarket;
import com.example.service.SupermarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/supermarkets")
public class SupermarketController {

    @Autowired
    private SupermarketService supermarketService;

    @GetMapping("/product")
    public ResponseEntity<ProductListSupermarket> searchProducts(
            @RequestParam(name = "q") String query,
            @RequestParam(name = "comuna", required = false) String comuna) {
        
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        ProductListSupermarket result = supermarketService.getProducts(query, comuna);
        return ResponseEntity.ok(result);
    }
}
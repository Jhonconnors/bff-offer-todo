package com.example.controller;


import com.example.model.farmacy.ProductListPharmacy;
import com.example.service.PharmacyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/farmacy")
public class PharmacyController {

    @Autowired
    private PharmacyService pharmacyService;

    // Endpoint configurado según tu Swagger
    @GetMapping("/medicament/product")
    @PreAuthorize("hasAuthority('read:data')")
    public ResponseEntity<ProductListPharmacy> getProducts(
            @RequestParam(name = "q") String query,
            @RequestParam(name = "comuna", required = false) String comuna
    ) {
        // Llamamos al servicio (al trabajador)
        ProductListPharmacy resultado = pharmacyService.getProducts(query, comuna);
        
        // Respondemos con HTTP 200 (OK) y los datos
        return ResponseEntity.ok(resultado);
    }
}
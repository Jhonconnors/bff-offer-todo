package com.example.model.supermarket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductSupermarket {
    private String nombre;
    private String descripcion;
    private String imagen;
    private String supermercadoUrl;
    private Precio precioUnitario; // Reutilizamos la clase Precio existente
}
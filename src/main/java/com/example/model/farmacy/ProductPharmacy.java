package com.example.model.farmacy;


public class ProductPharmacy {
    private String nombre;
    private String descripcion;
    private String imagen;
    private String farmaciaUrl;
    private Precio precioUnitario; // Aquí usamos la clase anterior

    public ProductPharmacy() {}

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public String getFarmaciaUrl() { return farmaciaUrl; }
    public void setFarmaciaUrl(String farmaciaUrl) { this.farmaciaUrl = farmaciaUrl; }

    public Precio getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(Precio precioUnitario) { this.precioUnitario = precioUnitario; }
}

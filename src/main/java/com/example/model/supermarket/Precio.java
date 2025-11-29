package com.example.model.supermarket;

public class Precio {
    private Double valor;
    private String moneda;

    // Constructores
    public Precio() {}
    public Precio(Double valor, String moneda) {
        this.valor = valor;
        this.moneda = moneda;
    }

    // Getters y Setters
    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }

    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
}


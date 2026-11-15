package com.app.mockitoapp.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad mapeada a la tabla "cargos".
 */
@Entity
@Table(name = "cargos")
public class Cargo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 100, unique = true)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "salario_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal salarioBase;

    protected Cargo() {
        // requerido por JPA
    }

    public Cargo(Integer id, String nombre, String descripcion, BigDecimal salarioBase) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.salarioBase = salarioBase;
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getSalarioBase() {
        return salarioBase;
    }

    @Override
    public String toString() {
        return nombre;
    }
}

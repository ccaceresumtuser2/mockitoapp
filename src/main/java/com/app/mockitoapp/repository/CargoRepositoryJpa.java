package com.app.mockitoapp.repository;

import java.util.Optional;

import com.app.mockitoapp.model.Cargo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * Implementación JPA del repositorio de cargos.
 */
public class CargoRepositoryJpa implements CargoRepository {

    private final EntityManagerFactory emf;

    public CargoRepositoryJpa(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Optional<Cargo> findById(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            return Optional.ofNullable(em.find(Cargo.class, id));
        }
    }
}

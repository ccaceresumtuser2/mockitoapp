package com.app.mockitoapp.repository;

import java.util.List;
import java.util.Optional;

import com.app.mockitoapp.model.Empleado;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

/**
 * Implementación JPA del repositorio de empleados (usa PostgreSQL real).
 */
public class EmpleadoRepositoryJpa implements EmpleadoRepository {

    private final EntityManagerFactory emf;

    public EmpleadoRepositoryJpa(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public List<Empleado> findAll() {
        try (EntityManager em = emf.createEntityManager()) {
            // JOIN FETCH carga el cargo en la misma consulta (evita LazyInitializationException)
            return em.createQuery(
                    "SELECT e FROM Empleado e JOIN FETCH e.cargo ORDER BY e.id", Empleado.class)
                    .getResultList();
        }
    }

    @Override
    public Optional<Empleado> findById(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                    "SELECT e FROM Empleado e JOIN FETCH e.cargo WHERE e.id = :id", Empleado.class)
                    .setParameter("id", id)
                    .getResultStream()
                    .findFirst();
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        try (EntityManager em = emf.createEntityManager()) {
            Long total = em.createQuery(
                    "SELECT COUNT(e) FROM Empleado e WHERE LOWER(e.email) = LOWER(:email)", Long.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return total > 0;
        }
    }

    @Override
    public Empleado save(Empleado empleado) {
        try (EntityManager em = emf.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                Empleado resultado;
                if (empleado.getId() == null) {
                    em.persist(empleado);   // INSERT
                    resultado = empleado;
                } else {
                    resultado = em.merge(empleado); // UPDATE
                }
                tx.commit();
                return resultado;
            } catch (RuntimeException e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                throw e;
            }
        }
    }
}

package com.app.mockitoapp.repository;

import java.util.List;
import java.util.Optional;

import com.app.mockitoapp.model.Empleado;

/**
 * Contrato de acceso a datos de empleados.
 * El servicio depende de esta interfaz, lo que permite reemplazarla por un mock en los tests.
 */
public interface EmpleadoRepository {

    List<Empleado> findAll();

    Optional<Empleado> findById(Integer id);

    boolean existsByEmail(String email);

    Empleado save(Empleado empleado);
}

package com.app.mockitoapp.repository;

import java.util.Optional;

import com.app.mockitoapp.model.Cargo;

/**
 * Contrato de acceso a datos de cargos.
 */
public interface CargoRepository {

    Optional<Cargo> findById(Integer id);
}

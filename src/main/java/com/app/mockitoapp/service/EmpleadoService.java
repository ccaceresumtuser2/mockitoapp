package com.app.mockitoapp.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import com.app.mockitoapp.model.Cargo;
import com.app.mockitoapp.model.Empleado;
import com.app.mockitoapp.repository.CargoRepository;
import com.app.mockitoapp.repository.EmpleadoRepository;

/**
 * Lógica de negocio de empleados.
 * Depende de interfaces de repositorio: en producción se usan las implementaciones JPA
 * y en los tests se reemplazan por mocks de Mockito.
 */
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final CargoRepository cargoRepository;

    public EmpleadoService(EmpleadoRepository empleadoRepository, CargoRepository cargoRepository) {
        this.empleadoRepository = empleadoRepository;
        this.cargoRepository = cargoRepository;
    }

    public List<Empleado> listar() {
        return empleadoRepository.findAll();
    }

    public Empleado buscarPorId(Integer id) {
        return empleadoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado: " + id));
    }

    /** Suma de los salarios de todos los empleados. */
    public BigDecimal totalNomina() {
        return empleadoRepository.findAll().stream()
                .map(Empleado::getSalario)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Reglas:
     * - nombre y apellido obligatorios
     * - el email no puede repetirse (se guarda en minúsculas)
     * - el cargo debe existir
     * - el salario no puede ser menor al salario base del cargo
     */
    public Empleado contratar(String nombre, String apellido, String email, String telefono,
                              Integer cargoId, BigDecimal salario) {
        if (nombre == null || nombre.isBlank() || apellido == null || apellido.isBlank()) {
            throw new IllegalArgumentException("Nombre y apellido son obligatorios");
        }
        String emailNormalizado = email.trim().toLowerCase();
        if (empleadoRepository.existsByEmail(emailNormalizado)) {
            throw new IllegalStateException("El email ya está registrado: " + emailNormalizado);
        }
        Cargo cargo = cargoRepository.findById(cargoId)
                .orElseThrow(() -> new IllegalArgumentException("Cargo no encontrado: " + cargoId));
        if (salario.compareTo(cargo.getSalarioBase()) < 0) {
            throw new IllegalArgumentException(
                    "El salario no puede ser menor al salario base del cargo (" + cargo.getSalarioBase() + ")");
        }

        Empleado nuevo = new Empleado(nombre, apellido, emailNormalizado, telefono,
                LocalDate.now(), salario, cargo);
        return empleadoRepository.save(nuevo);
    }

    /** Aumenta el salario de un empleado en un porcentaje (redondeado a 2 decimales). */
    public Empleado aumentarSalario(Integer id, BigDecimal porcentaje) {
        if (porcentaje == null || porcentaje.signum() <= 0) {
            throw new IllegalArgumentException("El porcentaje debe ser mayor a 0");
        }
        Empleado empleado = buscarPorId(id);
        BigDecimal factor = BigDecimal.ONE.add(porcentaje.divide(BigDecimal.valueOf(100)));
        empleado.setSalario(empleado.getSalario().multiply(factor).setScale(2, RoundingMode.HALF_UP));
        return empleadoRepository.save(empleado);
    }
}

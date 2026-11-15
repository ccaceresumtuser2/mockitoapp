package com.app.mockitoapp;

import com.app.mockitoapp.config.JpaUtil;
import com.app.mockitoapp.model.Empleado;
import com.app.mockitoapp.repository.CargoRepositoryJpa;
import com.app.mockitoapp.repository.EmpleadoRepositoryJpa;
import com.app.mockitoapp.service.EmpleadoService;

import jakarta.persistence.EntityManagerFactory;

/**
 * Punto de entrada: conecta con PostgreSQL (empleados_db) vía JPA y
 * muestra datos de la tabla empleados usando la capa de servicio.
 */
public class Mockitoapp {

    public static void main(String[] args) {
        EntityManagerFactory emf = JpaUtil.getEntityManagerFactory();
        try {
            // Armado manual de capas: repository (JPA) -> service
            EmpleadoService service = new EmpleadoService(
                    new EmpleadoRepositoryJpa(emf),
                    new CargoRepositoryJpa(emf));

            System.out.println("\n=== LISTADO DE EMPLEADOS ===");
            System.out.printf("%-3s %-25s %-22s %10s  %s%n", "ID", "NOMBRE", "CARGO", "SALARIO", "EMAIL");
            service.listar().forEach(System.out::println);

            System.out.println("\n=== BUSCAR EMPLEADO ID 3 ===");
            Empleado e = service.buscarPorId(3);
            System.out.println(e.getNombreCompleto() + " - " + e.getCargo().getNombre()
                    + " - ingreso: " + e.getFechaIngreso());

            System.out.println("\n=== TOTAL NÓMINA ===");
            System.out.println(service.totalNomina());
        } finally {
            JpaUtil.cerrar();
        }
    }
}

package com.app.mockitoapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.app.mockitoapp.model.Cargo;
import com.app.mockitoapp.model.Empleado;
import com.app.mockitoapp.repository.CargoRepository;
import com.app.mockitoapp.repository.EmpleadoRepository;

/**
 * Pruebas unitarias de EmpleadoService con Mockito + JUnit 5.
 * NO se conecta a PostgreSQL: los repositorios JPA se reemplazan por mocks.
 *
 * Estructura de cada test: Arrange (given) / Act (when) / Assert (then).
 */
@ExtendWith(MockitoExtension.class) // Inicializa @Mock, @InjectMocks y @Captor
class EmpleadoServiceTest {

    @Mock
    private EmpleadoRepository empleadoRepository;   // simulado

    @Mock
    private CargoRepository cargoRepository;         // simulado

    @InjectMocks
    private EmpleadoService service;                 // clase real con los mocks inyectados

    @Captor
    private ArgumentCaptor<Empleado> empleadoCaptor;

    private Cargo desarrolladorJunior;

    @BeforeEach
    void setUp() {
        desarrolladorJunior = new Cargo(6, "Desarrollador Junior", "Desarrollo", new BigDecimal("3000.00"));
    }

    private Empleado empleado(Integer id, String nombre, String salario) {
        Empleado e = new Empleado(nombre, "Pérez", nombre.toLowerCase() + "@empresa.com", "999",
                LocalDate.of(2020, 1, 1), new BigDecimal(salario), desarrolladorJunior);
        e.setId(id);
        return e;
    }

    // ---------------------------------------------------------------
    // 1) Stubbing básico: when(...).thenReturn(...)
    // ---------------------------------------------------------------
    @Test
    @DisplayName("buscarPorId devuelve el empleado cuando existe")
    void buscarPorId_existe() {
        when(empleadoRepository.findById(3)).thenReturn(Optional.of(empleado(3, "Carlos", "13500.00")));

        Empleado resultado = service.buscarPorId(3);

        assertEquals("Carlos Pérez", resultado.getNombreCompleto());
        verify(empleadoRepository).findById(3);
    }

    @Test
    @DisplayName("buscarPorId lanza excepción cuando no existe")
    void buscarPorId_noExiste() {
        when(empleadoRepository.findById(999)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.buscarPorId(999));

        assertEquals("Empleado no encontrado: 999", ex.getMessage());
    }

    // ---------------------------------------------------------------
    // 2) Stub que devuelve una lista
    // ---------------------------------------------------------------
    @Test
    @DisplayName("totalNomina suma los salarios de todos los empleados")
    void totalNomina() {
        when(empleadoRepository.findAll()).thenReturn(List.of(
                empleado(1, "Ana", "3000.00"),
                empleado(2, "Luis", "4500.50"),
                empleado(3, "Sofía", "2499.50")));

        assertEquals(new BigDecimal("10000.00"), service.totalNomina());
    }

    // ---------------------------------------------------------------
    // 3) thenAnswer + ArgumentCaptor
    // ---------------------------------------------------------------
    @Test
    @DisplayName("contratar guarda el empleado con email en minúsculas y cargo asignado")
    void contratar_ok() {
        when(empleadoRepository.existsByEmail("pedro.rojas@empresa.com")).thenReturn(false);
        when(cargoRepository.findById(6)).thenReturn(Optional.of(desarrolladorJunior));
        when(empleadoRepository.save(any(Empleado.class))).thenAnswer(inv -> {
            Empleado e = inv.getArgument(0);
            e.setId(100);                // simula el id generado por la BD
            return e;
        });

        Empleado resultado = service.contratar("Pedro", "Rojas", "  PEDRO.Rojas@Empresa.com ",
                "911", 6, new BigDecimal("3200.00"));

        assertEquals(100, resultado.getId());

        verify(empleadoRepository).save(empleadoCaptor.capture());
        Empleado guardado = empleadoCaptor.getValue();
        assertEquals("pedro.rojas@empresa.com", guardado.getEmail());
        assertEquals("Desarrollador Junior", guardado.getCargo().getNombre());
        assertNotNull(guardado.getFechaIngreso());
    }

    // ---------------------------------------------------------------
    // 4) verify(..., never()) cuando una regla corta el flujo
    // ---------------------------------------------------------------
    @Test
    @DisplayName("contratar falla si el email ya existe y no guarda nada")
    void contratar_emailDuplicado() {
        when(empleadoRepository.existsByEmail("carlos.mendoza@empresa.com")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> service.contratar(
                "Carlos", "Mendoza", "carlos.mendoza@empresa.com", null, 3, new BigDecimal("13000")));

        verify(empleadoRepository, never()).save(any());
        verify(cargoRepository, never()).findById(anyInt());
    }

    @Test
    @DisplayName("contratar falla si el salario es menor al salario base del cargo")
    void contratar_salarioBajoBase() {
        when(empleadoRepository.existsByEmail(anyString())).thenReturn(false);
        when(cargoRepository.findById(6)).thenReturn(Optional.of(desarrolladorJunior));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.contratar(
                "Ana", "Soto", "ana@empresa.com", null, 6, new BigDecimal("2500.00")));

        assertEquals("El salario no puede ser menor al salario base del cargo (3000.00)", ex.getMessage());
        verify(empleadoRepository, never()).save(any());
    }

    @Test
    @DisplayName("contratar falla si el cargo no existe")
    void contratar_cargoNoExiste() {
        when(empleadoRepository.existsByEmail(anyString())).thenReturn(false);
        when(cargoRepository.findById(77)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.contratar(
                "Ana", "Soto", "ana@empresa.com", null, 77, new BigDecimal("5000")));

        verify(empleadoRepository, times(1)).existsByEmail("ana@empresa.com");
        verify(empleadoRepository, never()).save(any());
    }

    // ---------------------------------------------------------------
    // 5) verifyNoInteractions: validación antes de tocar la BD
    // ---------------------------------------------------------------
    @Test
    @DisplayName("contratar con nombre vacío no consulta ningún repositorio")
    void contratar_nombreVacio() {
        assertThrows(IllegalArgumentException.class, () -> service.contratar(
                " ", "Soto", "x@empresa.com", null, 6, new BigDecimal("5000")));

        verifyNoInteractions(empleadoRepository, cargoRepository);
    }

    // ---------------------------------------------------------------
    // 6) Cálculo + captor en una actualización
    // ---------------------------------------------------------------
    @Test
    @DisplayName("aumentarSalario aplica el porcentaje y guarda el nuevo salario")
    void aumentarSalario_ok() {
        when(empleadoRepository.findById(5)).thenReturn(Optional.of(empleado(5, "Lucía", "8200.00")));
        when(empleadoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Empleado resultado = service.aumentarSalario(5, new BigDecimal("10"));

        assertEquals(new BigDecimal("9020.00"), resultado.getSalario());
        verify(empleadoRepository).save(empleadoCaptor.capture());
        assertEquals(5, empleadoCaptor.getValue().getId());
    }

    @Test
    @DisplayName("aumentarSalario con porcentaje inválido no consulta la BD")
    void aumentarSalario_porcentajeInvalido() {
        assertThrows(IllegalArgumentException.class,
                () -> service.aumentarSalario(5, BigDecimal.ZERO));

        verifyNoInteractions(empleadoRepository);
    }

    // ---------------------------------------------------------------
    // 7) thenThrow: simular una falla de la base de datos
    // ---------------------------------------------------------------
    @Test
    @DisplayName("si la base de datos falla al guardar, la excepción se propaga")
    void contratar_falloBaseDeDatos() {
        when(empleadoRepository.existsByEmail(anyString())).thenReturn(false);
        when(cargoRepository.findById(6)).thenReturn(Optional.of(desarrolladorJunior));
        when(empleadoRepository.save(any())).thenThrow(new RuntimeException("Conexión con PostgreSQL perdida"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.contratar(
                "Luis", "Díaz", "luis@empresa.com", null, 6, new BigDecimal("3000")));

        assertEquals("Conexión con PostgreSQL perdida", ex.getMessage());
    }
}

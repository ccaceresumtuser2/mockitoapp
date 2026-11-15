# mockitoapp

Aplicación de consola en Java 21 que gestiona empleados y cargos sobre PostgreSQL con JPA (Hibernate), y que muestra cómo probar la capa de servicio con **JUnit 5 + Mockito** sin conectarse a la base de datos.

## Tecnologías

| Herramienta | Versión |
|---|---|
| Java (JDK) | 21 |
| Maven | 3.9+ |
| Hibernate ORM (JPA) | 6.6.4.Final |
| PostgreSQL JDBC | 42.7.4 |
| JUnit Jupiter | 5.11.4 |
| Mockito (`mockito-junit-jupiter`) | 5.14.2 |
| JaCoCo | 0.8.12 |

## Estructura

```
src/
├── main/
│   ├── java/com/app/mockitoapp/
│   │   ├── Mockitoapp.java              # main: lista empleados, busca uno y calcula la nómina
│   │   ├── config/JpaUtil.java          # EntityManagerFactory única ("empleadosPU")
│   │   ├── model/                       # Entidades Cargo y Empleado
│   │   ├── repository/                  # Interfaces + implementaciones JPA
│   │   └── service/EmpleadoService.java # Lógica de negocio
│   └── resources/META-INF/persistence.xml
└── test/java/com/app/mockitoapp/service/
    └── EmpleadoServiceTest.java         # Tests unitarios con mocks
```

El servicio depende de las **interfaces** `EmpleadoRepository` y `CargoRepository`. En producción recibe las implementaciones JPA; en los tests, mocks de Mockito.

## Reglas de negocio (`EmpleadoService`)

- `listar()` – todos los empleados con su cargo.
- `buscarPorId(id)` – lanza `IllegalArgumentException` si no existe.
- `totalNomina()` – suma de los salarios.
- `contratar(...)`:
  - nombre y apellido obligatorios;
  - el email se normaliza (trim + minúsculas) y no puede repetirse;
  - el cargo debe existir;
  - el salario no puede ser menor al salario base del cargo.
- `aumentarSalario(id, porcentaje)` – el porcentaje debe ser > 0; el resultado se redondea a 2 decimales (HALF_UP).

## Base de datos

La aplicación espera una base `empleados_db` en `localhost:5432` con las tablas ya creadas (`hibernate.hbm2ddl.auto = none`):

- **cargos**: `id`, `nombre` (único), `descripcion`, `salario_base`
- **empleados**: `id`, `nombre`, `apellido`, `email` (único), `telefono`, `fecha_ingreso`, `salario`, `cargo_id` → `cargos.id`

La URL, el usuario y la contraseña se configuran en [persistence.xml](src/main/resources/META-INF/persistence.xml). Ajústalos a tu entorno local.

> Los tests **no** necesitan PostgreSQL; solo la ejecución del `main` lo requiere.

## Uso

Ejecutar los tests (genera también el reporte de cobertura):

```bash
mvn test
```

- Reportes de tests: `target/surefire-reports/`
- Cobertura JaCoCo: `target/site/jacoco/index.html`

Ejecutar la aplicación (requiere la base de datos):

```bash
mvn compile exec:java
```

## Técnicas de Mockito cubiertas en los tests

| # | Técnica | Ejemplo |
|---|---|---|
| 1 | Stubbing básico `when(...).thenReturn(...)` | `buscarPorId_existe`, `buscarPorId_noExiste` |
| 2 | Stub que devuelve una lista | `totalNomina` |
| 3 | `thenAnswer` + `ArgumentCaptor` | `contratar_ok` |
| 4 | `verify(..., never())` | `contratar_emailDuplicado`, `contratar_salarioBajoBase`, `contratar_cargoNoExiste` |
| 5 | `verifyNoInteractions` | `contratar_nombreVacio`, `aumentarSalario_porcentajeInvalido` |
| 6 | Cálculo + captor en una actualización | `aumentarSalario_ok` |
| 7 | `thenThrow` para simular fallas de BD | `contratar_falloBaseDeDatos` |

Mockito se carga como agente Java desde Surefire (`-javaagent`) para evitar las advertencias de *dynamic agent loading* en JDK 21+.

## Integración continua

[.github/workflows/ci.yml](.github/workflows/ci.yml) ejecuta `mvn -B clean verify` con JDK 21 (Temurin) en cada push o pull request a `main` y `dev`, y publica los reportes de Surefire como artefacto.

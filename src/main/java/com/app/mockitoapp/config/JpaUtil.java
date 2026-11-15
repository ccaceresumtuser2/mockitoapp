package com.app.mockitoapp.config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Crea una única EntityManagerFactory a partir de la unidad de persistencia
 * "empleadosPU" definida en META-INF/persistence.xml.
 */
public final class JpaUtil {

    private static final EntityManagerFactory EMF =
            Persistence.createEntityManagerFactory("empleadosPU");

    private JpaUtil() {
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        return EMF;
    }

    public static void cerrar() {
        if (EMF.isOpen()) {
            EMF.close();
        }
    }
}

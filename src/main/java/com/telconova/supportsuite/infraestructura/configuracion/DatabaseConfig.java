package com.telconova.supportsuite.infraestructura.configuracion;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuración de base de datos
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.repositorios"
)
public class DatabaseConfig {
}

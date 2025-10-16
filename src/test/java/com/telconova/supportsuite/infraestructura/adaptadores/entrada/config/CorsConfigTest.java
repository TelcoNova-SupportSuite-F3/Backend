package com.telconova.supportsuite.infraestructura.adaptadores.entrada.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para clase de configuración Cors")
class CorsConfigTest {

    @Test
    @DisplayName("Debe ejecutar método logCorsConfig en PostConstruct")
    void debeEjecutarLogCorsConfig() {
        // Arrange
        CorsConfig corsConfig = new CorsConfig();
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", "http://localhost:3000");
        ReflectionTestUtils.setField(corsConfig, "allowedMethods", "GET,POST");

        // Act & Assert - No lanza excepción
        assertThatCode(corsConfig::logCorsConfig).doesNotThrowAnyException();
    }
}

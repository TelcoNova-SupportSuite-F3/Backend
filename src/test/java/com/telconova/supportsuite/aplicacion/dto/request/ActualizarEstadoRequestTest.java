package com.telconova.supportsuite.aplicacion.dto.request;

import com.telconova.supportsuite.dominio.enums.EstadoOrden;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests para clase ActualizarEstadoRequest")
class ActualizarEstadoRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }


    @Test
    @DisplayName("Debe crear ActualizarEstadoRequest válido")
    void debeCrearActualizarEstadoRequestValido() {
        // Arrange & Act
        ActualizarEstadoRequest request = ActualizarEstadoRequest.builder()
                .nuevoEstado(EstadoOrden.EN_PROCESO)
                .observaciones("Iniciando trabajo")
                .fechaInicioTrabajo(LocalDateTime.now())
                .fechaFinTrabajo(LocalDateTime.now().plusHours(2))
                .build();

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.getNuevoEstado()).isEqualTo(EstadoOrden.EN_PROCESO);
        assertThat(request.getObservaciones()).isEqualTo("Iniciando trabajo");
        assertThat(request.getFechaInicioTrabajo()).isNotNull();
        assertThat(request.getFechaFinTrabajo()).isNotNull();
    }

    @Test
    @DisplayName("Debe rechazar ActualizarEstadoRequest sin estado")
    void debeRechazarActualizarEstadoRequestSinEstado() {
        // Arrange
        ActualizarEstadoRequest request = ActualizarEstadoRequest.builder()
                .nuevoEstado(null)
                .observaciones("Sin estado")
                .build();

        // Act
        Set<ConstraintViolation<ActualizarEstadoRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations)
                .isNotEmpty()
                .anyMatch(v -> v.getMessage().contains("El nuevo estado es obligatorio"));
    }

}

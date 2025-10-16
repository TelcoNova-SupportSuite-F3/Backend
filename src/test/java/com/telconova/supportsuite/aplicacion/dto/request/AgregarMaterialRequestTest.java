package com.telconova.supportsuite.aplicacion.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests para clase AgregarMaterialRequest")
class AgregarMaterialRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Debe crear AgregarMaterialRequest válido")
    void debeCrearAgregarMaterialRequestValido() {
        // Arrange & Act
        AgregarMaterialRequest request = AgregarMaterialRequest.builder()
                .materialId(1L)
                .cantidad(5)
                .build();
        // Assert
        assertThat(request).isNotNull();
        assertThat(request.getMaterialId()).isEqualTo(1L);
        assertThat(request.getCantidad()).isEqualTo(5);
        Set<ConstraintViolation<AgregarMaterialRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Debe rechazar AgregarMaterialRequest con materialId nulo")
    void debeRechazarAgregarMaterialRequestConMaterialIdNulo() {
        // Arrange
        AgregarMaterialRequest request = AgregarMaterialRequest.builder()
                .materialId(null)
                .cantidad(5)
                .build();
        // Act
        Set<ConstraintViolation<AgregarMaterialRequest>> violations = validator.validate(request);
        // Assert
        assertThat(violations).hasSize(1);
        ConstraintViolation<AgregarMaterialRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath()).hasToString("materialId");
        assertThat(violation.getMessage()).isEqualTo("El ID del material es obligatorio");
    }

    @Test
    @DisplayName("Debe rechazar AgregarMaterialRequest con cantidad negativa o cero")
    void debeRechazarAgregarMaterialRequestConCantidadNegativaOCero() {
        // Arrange
        AgregarMaterialRequest requestNegativo = AgregarMaterialRequest.builder()
                .materialId(1L)
                .cantidad(-3)
                .build();
        AgregarMaterialRequest requestCero = AgregarMaterialRequest.builder()
                .materialId(1L)
                .cantidad(0)
                .build();
        // Act
        Set<ConstraintViolation<AgregarMaterialRequest>> violationsNegativo = validator.validate(requestNegativo);
        Set<ConstraintViolation<AgregarMaterialRequest>> violationsCero = validator.validate(requestCero);
        // Assert
        assertThat(violationsNegativo).hasSize(1);
        ConstraintViolation<AgregarMaterialRequest> violationNegativo = violationsNegativo.iterator().next();
        assertThat(violationNegativo.getPropertyPath()).hasToString("cantidad");
        assertThat(violationNegativo.getMessage()).isEqualTo("La cantidad debe ser mayor a cero");
        assertThat(violationsCero).hasSize(1);
        ConstraintViolation<AgregarMaterialRequest> violationCero = violationsCero.iterator().next();
        assertThat(violationCero.getPropertyPath()).hasToString("cantidad");
        assertThat(violationCero.getMessage()).isEqualTo("La cantidad debe ser mayor a cero");
    }

    @Test
    @DisplayName("Debe rechazar AgregarMaterialRequest con cantidad nula")
    void debeRechazarAgregarMaterialRequestConCantidadNula() {
        // Arrange
        AgregarMaterialRequest request = AgregarMaterialRequest.builder()
                .materialId(1L)
                .cantidad(null)
                .build();
        // Act
        Set<ConstraintViolation<AgregarMaterialRequest>> violations = validator.validate(request);
        // Assert
        assertThat(violations).hasSize(1);
        ConstraintViolation<AgregarMaterialRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath()).hasToString("cantidad");
        assertThat(violation.getMessage()).isEqualTo("La cantidad es obligatoria");
    }

}

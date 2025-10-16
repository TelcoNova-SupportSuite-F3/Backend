package com.telconova.supportsuite.aplicacion.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests para clase LoginRequest ")
class LoginRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }


    @Test
    @DisplayName("Debe crear LoginRequest válido")
    void debeCrearLoginRequestValido() {
        // Arrange & Act
        LoginRequest request = LoginRequest.builder()
                .email("tecnico@telconova.com")
                .contrasena("password123")
                .build();

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.getEmail()).isEqualTo("tecnico@telconova.com");
        assertThat(request.getContrasena()).isEqualTo("password123");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Debe rechazar LoginRequest con email inválido")
    void debeRechazarLoginRequestConEmailInvalido() {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .email("emailinvalido")
                .contrasena("password123")
                .build();

        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations)
                .isNotEmpty()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    @DisplayName("Debe rechazar LoginRequest con dominio incorrecto")
    void debeRechazarLoginRequestConDominioIncorrecto() {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .email("usuario@gmail.com")
                .contrasena("password123")
                .build();

        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations)
                .isNotEmpty()
                .anyMatch(v -> v.getMessage().contains("Solo se permiten emails del dominio @telconova.com"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", " ", "   "})
    @DisplayName("Debe rechazar LoginRequest con email vacío o nulo")
    void debeRechazarLoginRequestConEmailVacioONulo(String email) {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .email(email)
                .contrasena("password123")
                .build();

        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isNotEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", " ", "   "})
    @DisplayName("Debe rechazar LoginRequest con contraseña vacía o nula")
    void debeRechazarLoginRequestConContrasenaVaciaONula(String contrasena) {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .email("tecnico@telconova.com")
                .contrasena(contrasena)
                .build();

        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isNotEmpty();
    }

}

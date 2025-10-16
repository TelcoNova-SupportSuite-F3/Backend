package com.telconova.supportsuite.aplicacion.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests para clase RegistrarEvidenciaRequest")
class RegistrarEvidenciaRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }


    @Test
    @DisplayName("Debe crear RegistrarEvidenciaRequest con comentario")
    void debeCrearRegistrarEvidenciaRequestConComentario() {
        // Arrange & Act
        RegistrarEvidenciaRequest request = RegistrarEvidenciaRequest.builder()
                .comentario("Trabajo completado exitosamente")
                .foto(null)
                .build();

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.getComentario()).isEqualTo("Trabajo completado exitosamente");
        assertThat(request.getFoto()).isNull();
        assertThat(request.tieneContenido()).isTrue();
    }

    @Test
    @DisplayName("Debe crear RegistrarEvidenciaRequest con foto")
    void debeCrearRegistrarEvidenciaRequestConFoto() {
        // Arrange
        MockMultipartFile foto = new MockMultipartFile(
                "foto",
                "evidencia.jpg",
                "image/jpeg",
                "contenido".getBytes()
        );

        // Act
        RegistrarEvidenciaRequest request = RegistrarEvidenciaRequest.builder()
                .comentario(null)
                .foto(foto)
                .build();

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.getComentario()).isNull();
        assertThat(request.getFoto()).isNotNull();
        assertThat(request.tieneContenido()).isTrue();
    }

    @Test
    @DisplayName("Debe crear RegistrarEvidenciaRequest con comentario y foto")
    void debeCrearRegistrarEvidenciaRequestConComentarioYFoto() {
        // Arrange
        MockMultipartFile foto = new MockMultipartFile(
                "foto",
                "evidencia.jpg",
                "image/jpeg",
                "contenido".getBytes()
        );

        // Act
        RegistrarEvidenciaRequest request = RegistrarEvidenciaRequest.builder()
                .comentario("Evidencia completa")
                .foto(foto)
                .build();

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.getComentario()).isEqualTo("Evidencia completa");
        assertThat(request.getFoto()).isNotNull();
        assertThat(request.tieneContenido()).isTrue();
    }

    @Test
    @DisplayName("Debe detectar request sin contenido")
    void debeDetectarRequestSinContenido() {
        // Arrange
        RegistrarEvidenciaRequest request = RegistrarEvidenciaRequest.builder()
                .comentario("")
                .foto(null)
                .build();

        // Act & Assert
        assertThat(request.tieneContenido()).isFalse();

        // Con espacios en blanco
        request.setComentario("   ");
        assertThat(request.tieneContenido()).isFalse();
    }

    @Test
    @DisplayName("Debe rechazar comentario muy largo")
    void debeRechazarComentarioMuyLargo() {
        // Arrange
        String comentarioLargo = "a".repeat(501);
        RegistrarEvidenciaRequest request = RegistrarEvidenciaRequest.builder()
                .comentario(comentarioLargo)
                .build();

        // Act
        Set<ConstraintViolation<RegistrarEvidenciaRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations)
                .isNotEmpty()
                .anyMatch(v -> v.getMessage().contains("El comentario no puede exceder 500 caracteres"));
    }
}

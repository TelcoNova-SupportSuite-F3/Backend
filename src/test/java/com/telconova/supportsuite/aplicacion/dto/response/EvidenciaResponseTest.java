package com.telconova.supportsuite.aplicacion.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests para clase EvidenciaResponse")
class EvidenciaResponseTest {

    @Test
    void debeCrearEvidenciaResponse() {
        // Arrange
        LocalDateTime fecha = LocalDateTime.now();

        // Act
        EvidenciaResponse response = EvidenciaResponse.builder()
                .id(1L)
                .tipo("FOTO")
                .contenido(null)
                .urlFoto("https://storage.com/foto.jpg")
                .nombreArchivo("evidencia.jpg")
                .tamanoArchivo("2.5 MB")
                .fechaCreacion(fecha)
                .creadoPor("tecnico@telconova.com")
                .build();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getTipo()).isEqualTo("FOTO");
        assertThat(response.getUrlFoto()).contains("storage.com");
        assertThat(response.getTamanoArchivo()).isEqualTo("2.5 MB");
    }
}

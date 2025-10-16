package com.telconova.supportsuite.aplicacion.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests para clase TecnicoResponse")
class TecnicoResponseTest {

    @Test
    void debeCrearTecnicoResponse() {
        // Arrange & Act
        TecnicoResponse response = TecnicoResponse.builder()
                .id(1L)
                .email("tecnico@telconova.com")
                .nombreCompleto("Pedro García")
                .activo(true)
                .build();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("tecnico@telconova.com");
        assertThat(response.getNombreCompleto()).isEqualTo("Pedro García");
        assertThat(response.isActivo()).isTrue();
    }
}

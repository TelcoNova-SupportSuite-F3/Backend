package com.telconova.supportsuite.aplicacion.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests para clase LoginResponse")
class LoginResponseTest {

    @Test
    void debeCrearLoginResponseCompleto() {
        // Arrange
        LocalDateTime expiracion = LocalDateTime.now().plusHours(8);

        // Act
        LoginResponse response = LoginResponse.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                .tipoToken("Bearer")
                .email("tecnico@telconova.com")
                .nombreCompleto("Juan Pérez")
                .rol("TECNICO")
                .expiracion(expiracion)
                .activo(true)
                .build();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).startsWith("eyJ");
        assertThat(response.getTipoToken()).isEqualTo("Bearer");
        assertThat(response.getEmail()).isEqualTo("tecnico@telconova.com");
        assertThat(response.getNombreCompleto()).isEqualTo("Juan Pérez");
        assertThat(response.getRol()).isEqualTo("TECNICO");
        assertThat(response.getExpiracion()).isEqualTo(expiracion);
        assertThat(response.isActivo()).isTrue();
    }
}

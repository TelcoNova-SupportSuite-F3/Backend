package com.telconova.supportsuite.compartido.constantes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Tests para ConfiguracionConstantes")
class ConfiguracionConstantesTest {

    @Test
    @DisplayName("Debe tener constantes JWT configuradas")
    void debeTenerConstantesJwtConfiguradas() {
        // Assert
        assertThat(ConfiguracionConstantes.JWT_HEADER).isEqualTo("Authorization");
        assertThat(ConfiguracionConstantes.JWT_PREFIX).isEqualTo("Bearer ");
        assertThat(ConfiguracionConstantes.JWT_CLAIM_ROL).isEqualTo("rol");
        assertThat(ConfiguracionConstantes.JWT_CLAIM_EMAIL).isEqualTo("email");
    }

    @Test
    @DisplayName("Debe tener constantes de archivo configuradas")
    void debeTenerConstantesArchivoConfiguradas() {
        // Assert

        assertThat(ConfiguracionConstantes.TIPOS_MIME_PERMITIDOS)
                .contains("image/jpeg", "image/jpg", "image/png");
        assertThat(ConfiguracionConstantes.DIRECTORIO_UPLOADS)
                .isEqualTo("uploads");
        assertThat(ConfiguracionConstantes.SUBDIRECTORIO_EVIDENCIAS)
                .isEqualTo("evidencias");
    }

    @Test
    @DisplayName("Debe tener constantes de validación configuradas")
    void debeTenerConstantesValidacionConfiguradas() {
        // Assert
        assertThat(ConfiguracionConstantes.DOMINIO_EMAIL_PERMITIDO).isEqualTo("@telconova.com");
        assertThat(ConfiguracionConstantes.LONGITUD_MAXIMA_COMENTARIO).isEqualTo(500);
        assertThat(ConfiguracionConstantes.LONGITUD_MAXIMA_TITULO).isEqualTo(255);
    }

    @Test
    @DisplayName("Debe tener constantes de tiempo configuradas")
    void debeTenerConstantesTiempoConfiguradas() {

        // Arrange
        long expiracionEsperada = 24L * 60 * 60 * 1000;
        int diasLimiteEsperados = 7;

        // Assert
        assertThat(expiracionEsperada)
                .isEqualTo(ConfiguracionConstantes.EXPIRACION_TOKEN_MILLIS);
        assertThat(diasLimiteEsperados)
                .isEqualTo(ConfiguracionConstantes.DIAS_LIMITE_ORDEN_VENCIDA);
    }

    @Test
    @DisplayName("Debe tener constantes de formato configuradas")
    void debeTenerConstantesFormatoConfiguradas() {
        // Assert
        assertThat(ConfiguracionConstantes.FORMATO_FECHA_API).isEqualTo("yyyy-MM-dd HH:mm:ss");
        assertThat(ConfiguracionConstantes.ZONA_HORARIA_COLOMBIA).isEqualTo("America/Bogota");
    }

    @Test
    @DisplayName("Debe tener constantes de cache configuradas")
    void debeTenerConstantesCacheConfiguradas() {
        // Assert
        assertThat(ConfiguracionConstantes.CACHE_USUARIOS).isEqualTo("usuarios");
        assertThat(ConfiguracionConstantes.CACHE_MATERIALES).isEqualTo("materiales");
        assertThat(ConfiguracionConstantes.CACHE_ORDENES).isEqualTo("ordenes");
    }
}

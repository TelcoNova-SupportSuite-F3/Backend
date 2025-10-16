package com.telconova.supportsuite.compartido.excepciones;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Tests para clase de ErrorResponse")
class ErrorResponseTest {

    @Test
    @DisplayName("Debe crear ErrorResponse con builder")
    void debeCrearErrorResponseConBuilder() {
        // Arrange
        LocalDateTime ahora = LocalDateTime.now();
        List<ErrorResponse.ErrorCampo> erroresCampos = Arrays.asList(
                ErrorResponse.ErrorCampo.builder()
                        .campo("email")
                        .valorRechazado("invalido")
                        .mensaje("Email no válido")
                        .build()
        );

        // Act
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(ahora)
                .status(400)
                .error("Bad Request")
                .message("Error de validación")
                .path("/api/test")
                .codigo("VALIDATION_ERROR")
                .detalles("Detalles adicionales")
                .erroresCampos(erroresCampos)
                .build();

        // Assert
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getTimestamp()).isEqualTo(ahora);
        assertThat(errorResponse.getStatus()).isEqualTo(400);
        assertThat(errorResponse.getError()).isEqualTo("Bad Request");
        assertThat(errorResponse.getMessage()).isEqualTo("Error de validación");
        assertThat(errorResponse.getPath()).isEqualTo("/api/test");
        assertThat(errorResponse.getCodigo()).isEqualTo("VALIDATION_ERROR");
        assertThat(errorResponse.getDetalles()).isEqualTo("Detalles adicionales");
        assertThat(errorResponse.getErroresCampos()).hasSize(1);
    }

    @Test
    @DisplayName("Debe crear ErrorResponse usando factory method of")
    void debeCrearErrorResponseUsandoFactoryMethodOf() {
        // Arrange
        String mensaje = "Error de prueba";
        int status = 404;
        String path = "/api/recurso";

        // Act
        ErrorResponse errorResponse = ErrorResponse.of(mensaje, status, path);

        // Assert
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus()).isEqualTo(404);
        assertThat(errorResponse.getError()).isEqualTo("Not Found");
        assertThat(errorResponse.getMessage()).isEqualTo(mensaje);
        assertThat(errorResponse.getPath()).isEqualTo(path);
        assertThat(errorResponse.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Debe crear ErrorResponse desde ApiExcepcion")
    void debeCrearErrorResponseDesdeApiExcepcion() {
        // Arrange
        ApiExcepcion excepcion = new ApiExcepcion(
                "Error de API",
                HttpStatus.FORBIDDEN,
                "ACCESO_DENEGADO",
                "Detalles del error"
        );
        String path = "/api/protegido";

        // Act
        ErrorResponse errorResponse = ErrorResponse.of(excepcion, path);

        // Assert
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus()).isEqualTo(403);
        assertThat(errorResponse.getError()).isEqualTo("Forbidden");
        assertThat(errorResponse.getMessage()).isEqualTo("Error de API");
        assertThat(errorResponse.getPath()).isEqualTo(path);
        assertThat(errorResponse.getCodigo()).isEqualTo("ACCESO_DENEGADO");
        assertThat(errorResponse.getDetalles()).isEqualTo("Detalles del error");
    }

    @Test
    @DisplayName("Debe obtener nombre de error correcto para diferentes códigos de estado")
    void debeObtenerNombreErrorCorrectoParaDiferentesCodigosEstado() {
        // Arrange & Act & Assert
        assertThat(ErrorResponse.of("msg", 400, "/").getError()).isEqualTo("Bad Request");
        assertThat(ErrorResponse.of("msg", 401, "/").getError()).isEqualTo("Unauthorized");
        assertThat(ErrorResponse.of("msg", 403, "/").getError()).isEqualTo("Forbidden");
        assertThat(ErrorResponse.of("msg", 404, "/").getError()).isEqualTo("Not Found");
        assertThat(ErrorResponse.of("msg", 409, "/").getError()).isEqualTo("Conflict");
        assertThat(ErrorResponse.of("msg", 422, "/").getError()).isEqualTo("Unprocessable Entity");
        assertThat(ErrorResponse.of("msg", 500, "/").getError()).isEqualTo("Internal Server Error");
        assertThat(ErrorResponse.of("msg", 503, "/").getError()).isEqualTo("Service Unavailable");
        assertThat(ErrorResponse.of("msg", 999, "/").getError()).isEqualTo("Unknown Error");
    }

    @Test
    @DisplayName("Debe crear ErrorCampo correctamente")
    void debeCrearErrorCampoCorrectamente() {
        // Arrange & Act
        ErrorResponse.ErrorCampo errorCampo = ErrorResponse.ErrorCampo.builder()
                .campo("telefono")
                .valorRechazado("123")
                .mensaje("Formato de teléfono inválido")
                .build();

        // Assert
        assertThat(errorCampo).isNotNull();
        assertThat(errorCampo.getCampo()).isEqualTo("telefono");
        assertThat(errorCampo.getValorRechazado()).isEqualTo("123");
        assertThat(errorCampo.getMensaje()).isEqualTo("Formato de teléfono inválido");
    }

    @Test
    @DisplayName("Debe crear ErrorResponse vacío con constructor sin argumentos")
    void debeCrearErrorResponseVacio() {
        // Act
        ErrorResponse errorResponse = new ErrorResponse();

        // Assert
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getTimestamp()).isNull();
        assertThat(errorResponse.getStatus()).isZero();
        assertThat(errorResponse.getError()).isNull();
    }

    @Test
    @DisplayName("Debe crear ErrorCampo vacío con constructor sin argumentos")
    void debeCrearErrorCampoVacio() {
        // Act
        ErrorResponse.ErrorCampo errorCampo = new ErrorResponse.ErrorCampo();

        // Assert
        assertThat(errorCampo).isNotNull();
        assertThat(errorCampo.getCampo()).isNull();
        assertThat(errorCampo.getValorRechazado()).isNull();
        assertThat(errorCampo.getMensaje()).isNull();
    }

    @Test
    @DisplayName("Debe crear ErrorResponse con constructor completo")
    void debeCrearErrorResponseConConstructorCompleto() {
        // Arrange
        LocalDateTime timestamp = LocalDateTime.now();
        List<ErrorResponse.ErrorCampo> errores = List.of(
                new ErrorResponse.ErrorCampo("campo1", "valor1", "mensaje1")
        );

        // Act
        ErrorResponse errorResponse = new ErrorResponse(
                timestamp,
                400,
                "Bad Request",
                "Mensaje de error",
                "/api/test",
                "ERROR_CODE",
                "Detalles",
                errores
        );

        // Assert
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getTimestamp()).isEqualTo(timestamp);
        assertThat(errorResponse.getStatus()).isEqualTo(400);
        assertThat(errorResponse.getError()).isEqualTo("Bad Request");
        assertThat(errorResponse.getMessage()).isEqualTo("Mensaje de error");
        assertThat(errorResponse.getPath()).isEqualTo("/api/test");
        assertThat(errorResponse.getCodigo()).isEqualTo("ERROR_CODE");
        assertThat(errorResponse.getDetalles()).isEqualTo("Detalles");
        assertThat(errorResponse.getErroresCampos()).hasSize(1);
    }

    @Test
    @DisplayName("Debe establecer y obtener valores usando setters")
    void debeEstablecerYObtenerValoresUsandoSetters() {
        // Arrange
        ErrorResponse errorResponse = new ErrorResponse();
        LocalDateTime ahora = LocalDateTime.now();

        // Act
        errorResponse.setTimestamp(ahora);
        errorResponse.setStatus(500);
        errorResponse.setError("Internal Server Error");
        errorResponse.setMessage("Error interno");
        errorResponse.setPath("/api/interno");
        errorResponse.setCodigo("INTERNAL_ERROR");
        errorResponse.setDetalles("Detalles del error");
        errorResponse.setErroresCampos(List.of());

        // Assert
        assertThat(errorResponse.getTimestamp()).isEqualTo(ahora);
        assertThat(errorResponse.getStatus()).isEqualTo(500);
        assertThat(errorResponse.getError()).isEqualTo("Internal Server Error");
        assertThat(errorResponse.getMessage()).isEqualTo("Error interno");
        assertThat(errorResponse.getPath()).isEqualTo("/api/interno");
        assertThat(errorResponse.getCodigo()).isEqualTo("INTERNAL_ERROR");
        assertThat(errorResponse.getDetalles()).isEqualTo("Detalles del error");
        assertThat(errorResponse.getErroresCampos()).isEmpty();
    }
}

package com.telconova.supportsuite.compartido.excepciones;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests para clase ApiExcepcion")
class ApiExcepcionTest {

    @Test
    @DisplayName("Debe crear ApiExcepcion con mensaje y estado")
    void debeCrearApiExcepcionConMensajeYEstado() {
        // Arrange
        String mensaje = "Error de prueba";
        HttpStatus estado = HttpStatus.BAD_REQUEST;

        // Act
        ApiExcepcion excepcion = new ApiExcepcion(mensaje, estado);

        // Assert
        assertThat(excepcion).isNotNull();
        assertThat(excepcion.getMessage()).isEqualTo(mensaje);
        assertThat(excepcion.getEstado()).isEqualTo(estado);
        assertThat(excepcion.getCodigo()).isEqualTo("BAD_REQUEST");
        assertThat(excepcion.getDetalles()).isNull();
    }

    @Test
    @DisplayName("Debe crear ApiExcepcion con mensaje, estado y código")
    void debeCrearApiExcepcionConMensajeEstadoYCodigo() {
        // Arrange
        String mensaje = "Error personalizado";
        HttpStatus estado = HttpStatus.NOT_FOUND;
        String codigo = "RECURSO_NO_ENCONTRADO";

        // Act
        ApiExcepcion excepcion = new ApiExcepcion(mensaje, estado, codigo);

        // Assert
        assertThat(excepcion).isNotNull();
        assertThat(excepcion.getMessage()).isEqualTo(mensaje);
        assertThat(excepcion.getEstado()).isEqualTo(estado);
        assertThat(excepcion.getCodigo()).isEqualTo(codigo);
        assertThat(excepcion.getDetalles()).isNull();
    }

    @Test
    @DisplayName("Debe crear ApiExcepcion con todos los parámetros")
    void debeCrearApiExcepcionConTodosLosParametros() {
        // Arrange
        String mensaje = "Error completo";
        HttpStatus estado = HttpStatus.CONFLICT;
        String codigo = "CONFLICTO_DATOS";
        Object detalles = new Object() {
            public final String campo = "email";
            public final String valor = "duplicado@test.com";
        };

        // Act
        ApiExcepcion excepcion = new ApiExcepcion(mensaje, estado, codigo, detalles);

        // Assert
        assertThat(excepcion).isNotNull();
        assertThat(excepcion.getMessage()).isEqualTo(mensaje);
        assertThat(excepcion.getEstado()).isEqualTo(estado);
        assertThat(excepcion.getCodigo()).isEqualTo(codigo);
        assertThat(excepcion.getDetalles()).isNotNull();
    }

    @Test
    @DisplayName("Debe crear excepción Bad Request usando factory method")
    void debeCrearBadRequestUsandoFactoryMethod() {
        // Arrange
        String mensaje = "Solicitud incorrecta";

        // Act
        ApiExcepcion excepcion = ApiExcepcion.badRequest(mensaje);

        // Assert
        assertThat(excepcion).isNotNull();
        assertThat(excepcion.getMessage()).isEqualTo(mensaje);
        assertThat(excepcion.getEstado()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Debe crear excepción Unauthorized usando factory method")
    void debeCrearUnauthorizedUsandoFactoryMethod() {
        // Arrange
        String mensaje = "No autorizado";

        // Act
        ApiExcepcion excepcion = ApiExcepcion.unauthorized(mensaje);

        // Assert
        assertThat(excepcion).isNotNull();
        assertThat(excepcion.getMessage()).isEqualTo(mensaje);
        assertThat(excepcion.getEstado()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Debe crear excepción Forbidden usando factory method")
    void debeCrearForbiddenUsandoFactoryMethod() {
        // Arrange
        String mensaje = "Acceso prohibido";

        // Act
        ApiExcepcion excepcion = ApiExcepcion.forbidden(mensaje);

        // Assert
        assertThat(excepcion).isNotNull();
        assertThat(excepcion.getMessage()).isEqualTo(mensaje);
        assertThat(excepcion.getEstado()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Debe crear excepción Not Found usando factory method")
    void debeCrearNotFoundUsandoFactoryMethod() {
        // Arrange
        String mensaje = "No encontrado";

        // Act
        ApiExcepcion excepcion = ApiExcepcion.notFound(mensaje);

        // Assert
        assertThat(excepcion).isNotNull();
        assertThat(excepcion.getMessage()).isEqualTo(mensaje);
        assertThat(excepcion.getEstado()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Debe crear excepción Conflict usando factory method")
    void debeCrearConflictUsandoFactoryMethod() {
        // Arrange
        String mensaje = "Conflicto de datos";

        // Act
        ApiExcepcion excepcion = ApiExcepcion.conflict(mensaje);

        // Assert
        assertThat(excepcion).isNotNull();
        assertThat(excepcion.getMessage()).isEqualTo(mensaje);
        assertThat(excepcion.getEstado()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("Debe crear excepción Internal Server Error usando factory method")
    void debeCrearInternalServerErrorUsandoFactoryMethod() {
        // Arrange
        String mensaje = "Error interno del servidor";

        // Act
        ApiExcepcion excepcion = ApiExcepcion.internalServerError(mensaje);

        // Assert
        assertThat(excepcion).isNotNull();
        assertThat(excepcion.getMessage()).isEqualTo(mensaje);
        assertThat(excepcion.getEstado()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("Debe crear excepción Service Unavailable usando factory method")
    void debeCrearServiceUnavailableUsandoFactoryMethod() {
        // Arrange
        String mensaje = "Servicio no disponible";

        // Act
        ApiExcepcion excepcion = ApiExcepcion.serviceUnavailable(mensaje);

        // Assert
        assertThat(excepcion).isNotNull();
        assertThat(excepcion.getMessage()).isEqualTo(mensaje);
        assertThat(excepcion.getEstado()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }
}

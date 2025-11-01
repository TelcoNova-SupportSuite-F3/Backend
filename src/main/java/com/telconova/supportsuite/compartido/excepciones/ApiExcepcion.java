package com.telconova.supportsuite.compartido.excepciones;


import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Excepción personalizada para errores de API
 */
@Getter
public class ApiExcepcion extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final HttpStatus estado;
    private final String codigo;
    private final transient Object detalles;

    public ApiExcepcion(String mensaje, HttpStatus estado) {
        super(mensaje);
        this.estado = estado;
        this.codigo = estado.name();
        this.detalles = null;
    }

    public ApiExcepcion(String mensaje, HttpStatus estado, String codigo) {
        super(mensaje);
        this.estado = estado;
        this.codigo = codigo;
        this.detalles = null;
    }

    public ApiExcepcion(String mensaje, HttpStatus estado, String codigo, Object detalles) {
        super(mensaje);
        this.estado = estado;
        this.codigo = codigo;
        this.detalles = detalles;
    }

    // Factory methods para errores comunes
    public static ApiExcepcion badRequest(String mensaje) {
        return new ApiExcepcion(mensaje, HttpStatus.BAD_REQUEST);
    }

    public static ApiExcepcion unauthorized(String mensaje) {
        return new ApiExcepcion(mensaje, HttpStatus.UNAUTHORIZED);
    }

    public static ApiExcepcion forbidden(String mensaje) {
        return new ApiExcepcion(mensaje, HttpStatus.FORBIDDEN);
    }

    public static ApiExcepcion notFound(String mensaje) {
        return new ApiExcepcion(mensaje, HttpStatus.NOT_FOUND);
    }

    public static ApiExcepcion conflict(String mensaje) {
        return new ApiExcepcion(mensaje, HttpStatus.CONFLICT);
    }

    public static ApiExcepcion internalServerError(String mensaje) {
        return new ApiExcepcion(mensaje, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static ApiExcepcion serviceUnavailable(String mensaje) {
        return new ApiExcepcion(mensaje, HttpStatus.SERVICE_UNAVAILABLE);
    }
}

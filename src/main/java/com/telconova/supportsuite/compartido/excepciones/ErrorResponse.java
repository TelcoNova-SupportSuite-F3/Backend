package com.telconova.supportsuite.compartido.excepciones;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Estructura de respuesta para errores de API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    private int status;
    private String error;
    private String message;
    private String path;
    private String codigo;
    private Object detalles;
    private List<ErrorCampo> erroresCampos;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorCampo {
        private String campo;
        private Object valorRechazado;
        private String mensaje;
    }

    // Factory methods para crear respuestas de error comunes
    public static ErrorResponse of(String mensaje, int status, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(getErrorName(status))
                .message(mensaje)
                .path(path)
                .build();
    }

    public static ErrorResponse of(ApiExcepcion ex, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(ex.getEstado().value())
                .error(ex.getEstado().getReasonPhrase())
                .message(ex.getMessage())
                .path(path)
                .codigo(ex.getCodigo())
                .detalles(ex.getDetalles())
                .build();
    }

    private static String getErrorName(int status) {
        return switch (status) {
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 409 -> "Conflict";
            case 422 -> "Unprocessable Entity";
            case 500 -> "Internal Server Error";
            case 503 -> "Service Unavailable";
            default -> "Unknown Error";
        };
    }
}

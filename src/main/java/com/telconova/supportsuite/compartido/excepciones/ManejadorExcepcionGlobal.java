package com.telconova.supportsuite.compartido.excepciones;

import com.telconova.supportsuite.compartido.constantes.MensajesConstantes;
import com.telconova.supportsuite.dominio.excepciones.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Manejador global de excepciones para la aplicación
 *
 * @author TelcoNova Development Team
 */
@Slf4j
@RestControllerAdvice
public class ManejadorExcepcionGlobal {

    // =====================================================
    // EXCEPCIONES DE DOMINIO
    // =====================================================

    @ExceptionHandler(UsuarioNoValidoExcepcion.class)
    public ResponseEntity<ErrorResponse> manejarUsuarioNoValido(
            UsuarioNoValidoExcepcion ex, WebRequest request) {
        log.warn("Error de usuario no válido: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message(ex.getMessage())
                .path(obtenerRuta(request))
                .codigo("USUARIO_NO_VALIDO")
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(OrdenNoEncontradaExcepcion.class)
    public ResponseEntity<ErrorResponse> manejarOrdenNoEncontrada(
            OrdenNoEncontradaExcepcion ex, WebRequest request) {
        log.warn("Orden no encontrada: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(obtenerRuta(request))
                .codigo("ORDEN_NO_ENCONTRADA")
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(EstadoOrdenInvalidoExcepcion.class)
    public ResponseEntity<ErrorResponse> manejarEstadoOrdenInvalido(
            EstadoOrdenInvalidoExcepcion ex, WebRequest request) {
        log.warn("Estado de orden inválido: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(obtenerRuta(request))
                .codigo("ESTADO_ORDEN_INVALIDO")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MaterialNoValidoExcepcion.class)
    public ResponseEntity<ErrorResponse> manejarMaterialNoValido(
            MaterialNoValidoExcepcion ex, WebRequest request) {
        log.warn("Material no válido: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(obtenerRuta(request))
                .codigo("MATERIAL_NO_VALIDO")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(EvidenciaNoValidaExcepcion.class)
    public ResponseEntity<ErrorResponse> manejarEvidenciaNoValida(
            EvidenciaNoValidaExcepcion ex, WebRequest request) {
        log.warn("Evidencia no válida: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(obtenerRuta(request))
                .codigo("EVIDENCIA_NO_VALIDA")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(AccesoNoAutorizadoExcepcion.class)
    public ResponseEntity<ErrorResponse> manejarAccesoNoAutorizado(
            AccesoNoAutorizadoExcepcion ex, WebRequest request) {
        log.warn("Acceso no autorizado: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message(ex.getMessage())
                .path(obtenerRuta(request))
                .codigo("ACCESO_NO_AUTORIZADO")
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(DominioExcepcion.class)
    public ResponseEntity<ErrorResponse> manejarExcepcionDominio(
            DominioExcepcion ex, WebRequest request) {
        log.warn("Error de dominio: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(obtenerRuta(request))
                .codigo("ERROR_DOMINIO")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(OrdenFinalizadaExcepcion.class)
    public ResponseEntity<ErrorResponse> manejarOrdenFinalizada(OrdenFinalizadaExcepcion ex) {
        log.warn("Intento de modificar orden finalizada: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Orden Finalizada")
                .message(ex.getMessage())
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    // =====================================================
    // EXCEPCIONES DE SEGURIDAD
    // =====================================================

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> manejarErrorAutenticacion(
            AuthenticationException ex, WebRequest request) {
        log.warn("Error de autenticación: {}", ex.getMessage());

        String mensaje = MensajesConstantes.ERROR_CREDENCIALES_INVALIDAS;
        if (ex instanceof BadCredentialsException) {
            mensaje = MensajesConstantes.ERROR_CREDENCIALES_INVALIDAS;
        } else if (ex instanceof DisabledException) {
            mensaje = MensajesConstantes.ERROR_USUARIO_INACTIVO;
        }

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message(mensaje)
                .path(obtenerRuta(request))
                .codigo("ERROR_AUTENTICACION")
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> manejarAccesoDenegado(
            AccessDeniedException ex, WebRequest request) {
        log.warn("Acceso denegado: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message(MensajesConstantes.ERROR_ACCESO_DENEGADO)
                .path(obtenerRuta(request))
                .codigo("ACCESO_DENEGADO")
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    // =====================================================
    // EXCEPCIONES DE VALIDACIÓN
    // =====================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarErrorValidacion(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("Error de validación de argumentos: {}", ex.getMessage());

        List<ErrorResponse.ErrorCampo> erroresCampos = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::convertirFieldError)
                .toList();

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Error de validación en los datos enviados")
                .path(obtenerRuta(request))
                .codigo("ERROR_VALIDACION")
                .erroresCampos(erroresCampos)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> manejarViolacionConstraints(
            ConstraintViolationException ex, WebRequest request) {
        log.warn("Violación de constraints: {}", ex.getMessage());

        List<ErrorResponse.ErrorCampo> erroresCampos = ex.getConstraintViolations()

                .stream()
                .map(this::convertirConstraintViolation)
                .toList();

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Error de validación de constraints")
                .path(obtenerRuta(request))
                .codigo("CONSTRAINT_VIOLATION")
                .erroresCampos(erroresCampos)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // =====================================================
    // EXCEPCIONES DE ARCHIVOS
    // =====================================================

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> manejarArchivoMuyGrande(
            MaxUploadSizeExceededException ex, WebRequest request) {
        log.warn("Archivo demasiado grande: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.PAYLOAD_TOO_LARGE.value())
                .error(HttpStatus.PAYLOAD_TOO_LARGE.getReasonPhrase())
                .message("El archivo es demasiado grande. Tamaño máximo permitido: 10MB")
                .path(obtenerRuta(request))
                .codigo("ARCHIVO_MUY_GRANDE")
                .build();

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(error);
    }

    // =====================================================
    // EXCEPCIONES GENERALES
    // =====================================================

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> manejarArgumentoIlegal(
            IllegalArgumentException ex, WebRequest request) {
        log.warn("Argumento ilegal: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(obtenerRuta(request))
                .codigo("ARGUMENTO_ILEGAL")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> manejarEndpointNoEncontrado(
            NoHandlerFoundException ex, WebRequest request) {
        log.warn("Endpoint no encontrado: {}", ex.getRequestURL());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message("El endpoint solicitado no existe")
                .path(ex.getRequestURL())
                .codigo("ENDPOINT_NO_ENCONTRADO")
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> manejarMensajeNoLegible(
            HttpMessageNotReadableException ex, WebRequest request) {
        log.warn("Mensaje HTTP no legible: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("El formato del JSON no es válido")
                .path(obtenerRuta(request))
                .codigo("JSON_MALFORMADO")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ApiExcepcion.class)
    public ResponseEntity<ErrorResponse> manejarApiExcepcion(
            ApiExcepcion ex, WebRequest request) {
        log.warn("Error de API: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.of(ex, obtenerRuta(request));
        return ResponseEntity.status(ex.getEstado()).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarErrorGeneral(
            Exception ex, WebRequest request) {
        log.error("Error interno del servidor", ex);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message(MensajesConstantes.ERROR_INTERNO_SERVIDOR)
                .path(obtenerRuta(request))
                .codigo("ERROR_INTERNO")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // =====================================================
    // MÉTODOS AUXILIARES
    // =====================================================

    private String obtenerRuta(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }

    private ErrorResponse.ErrorCampo convertirFieldError(FieldError fieldError) {
        return ErrorResponse.ErrorCampo.builder()
                .campo(fieldError.getField())
                .valorRechazado(fieldError.getRejectedValue())
                .mensaje(fieldError.getDefaultMessage())
                .build();
    }

    private ErrorResponse.ErrorCampo convertirConstraintViolation(ConstraintViolation<?> violation) {
        String campo = violation.getPropertyPath().toString();
        // Extraer solo el nombre del campo (último elemento del path)
        if (campo.contains(".")) {
            campo = campo.substring(campo.lastIndexOf(".") + 1);
        }

        return ErrorResponse.ErrorCampo.builder()
                .campo(campo)
                .valorRechazado(violation.getInvalidValue())
                .mensaje(violation.getMessage())
                .build();
    }
}

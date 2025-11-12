package com.telconova.supportsuite.infraestructura.configuracion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telconova.supportsuite.aplicacion.puertos.salida.INotificacionService;
import com.telconova.supportsuite.compartido.excepciones.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Manejador para errores de autenticación JWT
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final INotificacionService notificacionService;


    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // Capturar información del intento
        String ipAddress = obtenerIpCliente(request);
        String requestUrl = request.getRequestURL().toString();
        String method = request.getMethod();
        String userAgent = request.getHeader("User-Agent");
        String authHeader = request.getHeader("Authorization");
        LocalDateTime timestamp = LocalDateTime.now();

        // Determinar el motivo del rechazo
        String motivoRechazo = authHeader == null || authHeader.isEmpty()
                ? "Sin token de autenticación"
                : "Token inválido o expirado";

        // Log detallado
        log.warn("⚠️ Intento de acceso no autorizado - IP: {}, URL: {}, Método: {}, Motivo: {}",
                ipAddress, requestUrl, method, motivoRechazo);

        // Enviar notificación por email de forma asíncrona para no bloquear la respuesta
        try {
            notificacionService.enviarNotificacionAccesoNoAutorizado(
                    ipAddress,
                    requestUrl,
                    method,
                    userAgent,
                    motivoRechazo,
                    timestamp
            );
        } catch (Exception e) {
            // No fallar si el email falla, solo loguearlo
            log.error("Error al enviar notificación de acceso no autorizado: {}", e.getMessage());
        }

        // Construir respuesta de error
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(timestamp)
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message("Token de autenticación requerido o inválido")
                .path(request.getRequestURI())
                .codigo("TOKEN_REQUERIDO")
                .build();

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }

    /**
     * Obtiene la IP real del cliente, considerando proxies
     */
    private String obtenerIpCliente(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }

        String xrHeader = request.getHeader("X-Real-IP");
        if (xrHeader != null && !xrHeader.isEmpty()) {
            return xrHeader;
        }

        return request.getRemoteAddr();
    }
}

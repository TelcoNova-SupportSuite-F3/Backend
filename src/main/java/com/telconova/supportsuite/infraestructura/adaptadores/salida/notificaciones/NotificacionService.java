package com.telconova.supportsuite.infraestructura.adaptadores.salida.notificaciones;

import com.telconova.supportsuite.aplicacion.dto.response.CambioEstadoOrdenDTO;
import com.telconova.supportsuite.aplicacion.puertos.salida.INotificacionService;
import com.telconova.supportsuite.aplicacion.puertos.salida.INotificacionStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Servicio de notificaciones que orquesta el envío por diferentes canales
 */
@Slf4j
@Service
public class NotificacionService implements INotificacionService {


    private final INotificacionStrategy emailStrategy;
    private final INotificacionStrategy whatsappStrategy;
    private static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss";

    @Value("${app.notificaciones.supervisor-email}")
    private String supervisorEmail;

    public NotificacionService(
            @Qualifier("sendgridNotificacion") INotificacionStrategy emailStrategy,
            @Qualifier("whatsappNotificacion") INotificacionStrategy whatsappStrategy) {
        this.emailStrategy = emailStrategy;
        this.whatsappStrategy = whatsappStrategy;
    }

    @Override
    @Async
    public void notificarCambioEstadoASupervisor(CambioEstadoOrdenDTO cambioEstado) {
        log.info("Enviando notificación al supervisor para orden: {}", cambioEstado.getNumeroOrden());

        if (!emailStrategy.estaHabilitada()) {
            log.warn("Email no configurado. No se enviará notificación al supervisor.");
            return;
        }

        try {
            String asunto = String.format("Cambio de Estado - Orden %s", cambioEstado.getNumeroOrden());
            String mensaje = construirMensajeSupervisor(cambioEstado);

            emailStrategy.enviar(supervisorEmail, asunto, mensaje);
        } catch (Exception e) {
            log.error("Error al notificar al supervisor: {}", e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void notificarCambioEstadoACliente(CambioEstadoOrdenDTO cambioEstado) {
        log.info("Enviando notificación al cliente para orden: {}", cambioEstado.getNumeroOrden());

        if (cambioEstado.getClienteTelefono() == null || cambioEstado.getClienteTelefono().isEmpty()) {
            log.warn("Cliente sin teléfono registrado. No se enviará WhatsApp.");
            return;
        }

        if (!whatsappStrategy.estaHabilitada()) {
            log.warn("WhatsApp no configurado. No se enviará notificación al cliente.");
            return;
        }

        try {
            String mensaje = construirMensajeCliente(cambioEstado);
            whatsappStrategy.enviar(cambioEstado.getClienteTelefono(), null, mensaje);
        } catch (Exception e) {
            log.error("Error al notificar al cliente: {}", e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void enviarNotificacionAccesoNoAutorizado(
            String ipAddress,
            String urlSolicitada,
            String metodoHttp,
            String userAgent,
            String motivoRechazo,
            LocalDateTime timestamp) {

        try {
            String asunto = "🚨 ALERTA: Intento de acceso no autorizado - Telconova Support Suite";

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
            String fechaFormateada = timestamp.format(formatter);

            String mensaje = String.format("""
                ⚠️ ALERTA DE SEGURIDAD - ACCESO NO AUTORIZADO
                
                Se ha detectado un intento de acceso no autorizado al sistema Telconova Support Suite.
                
                📍 DETALLES DEL INTENTO:
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                📅 Fecha y hora: %s
                🌐 Dirección IP: %s
                🔗 URL solicitada: %s
                📡 Método HTTP: %s
                🚫 Motivo de rechazo: %s
                💻 User Agent: %s
                
                🔒 ACCIÓN TOMADA:
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                El acceso fue denegado automáticamente (401 Unauthorized).
                No se permitió el acceso al recurso protegido.
                
                ⚡ ACCIONES RECOMENDADAS:
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                • Revisar los logs del sistema para más detalles
                • Verificar si la IP es conocida o sospechosa
                • Si hay múltiples intentos, considerar bloquear la IP
                • Verificar patrones de ataque en el firewall
                
                📊 INFORMACIÓN TÉCNICA:
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                Sistema: Telconova Support Suite
                Módulo: Autenticación y Seguridad
                Código de error: TOKEN_REQUERIDO
                
                ---
                Este es un mensaje automático del sistema de seguridad.
                No responder a este correo.
                """,
                    fechaFormateada, ipAddress, urlSolicitada, metodoHttp,
                    motivoRechazo, userAgent != null ? userAgent : "No disponible"
            );

                emailStrategy.enviar(supervisorEmail, asunto, mensaje);
                log.info("✅ Notificación de seguridad enviada al admin: {}", supervisorEmail);




        } catch (Exception e) {
            log.error("Error al enviar notificación de acceso no autorizado: {}", e.getMessage(), e);
        }
    }

    private String construirMensajeSupervisor(CambioEstadoOrdenDTO cambioEstado) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

        return String.format("""
                ═══════════════════════════════════════
                📋 NOTIFICACIÓN DE CAMBIO DE ESTADO
                ═══════════════════════════════════════
                
                Orden: %s
                Técnico: %s
                Estado Anterior: %s
                Estado Nuevo: %s
                Fecha y Hora: %s
                
                ═══════════════════════════════════════
                Sistema TelcoNova - Support Suite
                """,
                cambioEstado.getNumeroOrden(),
                cambioEstado.getNombreTecnico(),
                cambioEstado.getEstadoAnterior(),
                cambioEstado.getEstadoNuevo(),
                cambioEstado.getFechaHoraCambio().format(formatter)
        );
    }

    private String construirMensajeCliente(CambioEstadoOrdenDTO cambioEstado) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

        return String.format("""
                Hola %s,
                
                Le informamos que su orden de servicio ha sido actualizada:
                
                🔹 Orden: %s
                🔹 Técnico: %s
                🔹 Estado Anterior: %s
                🔹 Estado Actual: %s
                🔹 Fecha: %s
                
                Gracias por confiar en TelcoNova.
                """,
                cambioEstado.getClienteNombre(),
                cambioEstado.getNumeroOrden(),
                cambioEstado.getNombreTecnico(),
                cambioEstado.getEstadoAnterior(),
                cambioEstado.getEstadoNuevo(),
                cambioEstado.getFechaHoraCambio().format(formatter)
        );
    }

}

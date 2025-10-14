package com.telconova.supportsuite.infraestructura.adaptadores.salida.notificaciones;

import com.telconova.supportsuite.aplicacion.dto.response.CambioEstadoOrdenDTO;
import com.telconova.supportsuite.aplicacion.puertos.salida.INotificacionService;
import com.telconova.supportsuite.aplicacion.puertos.salida.INotificacionStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Servicio de notificaciones que orquesta el envío por diferentes canales
 */
@Slf4j
@Service
public class NotificacionService implements INotificacionService {


    private final INotificacionStrategy emailStrategy;
    private final INotificacionStrategy whatsappStrategy;

    @Value("${app.notificaciones.supervisor-email}")
    private String supervisorEmail;

    public NotificacionService(
            @Qualifier("emailNotificacion") INotificacionStrategy emailStrategy,
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

    private String construirMensajeSupervisor(CambioEstadoOrdenDTO cambioEstado) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

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

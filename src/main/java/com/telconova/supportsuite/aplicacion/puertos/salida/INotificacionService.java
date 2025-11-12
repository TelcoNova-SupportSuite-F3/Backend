package com.telconova.supportsuite.aplicacion.puertos.salida;


import com.telconova.supportsuite.aplicacion.dto.response.CambioEstadoOrdenDTO;

import java.time.LocalDateTime;

/**
 * Puerto de salida para el envío de notificaciones
 */
public interface INotificacionService {
    /**
     * Envía notificación de cambio de estado al supervisor
     */
    void notificarCambioEstadoASupervisor(CambioEstadoOrdenDTO cambioEstado);

    /**
     * Envía notificación de cambio de estado al cliente
     */
    void notificarCambioEstadoACliente(CambioEstadoOrdenDTO cambioEstado);

    /**
     * Envía notificación de intento de acceso no autorizado
     */
    void enviarNotificacionAccesoNoAutorizado(
            String ipAddress,
            String urlSolicitada,
            String metodoHttp,
            String userAgent,
            String motivoRechazo,
            LocalDateTime timestamp
    );
}

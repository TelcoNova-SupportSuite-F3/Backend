package com.telconova.supportsuite.aplicacion.puertos.salida;


import com.telconova.supportsuite.aplicacion.dto.response.CambioEstadoOrdenDTO;

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
}

package com.telconova.supportsuite.aplicacion.puertos.salida;

public interface INotificacionStrategy {

    /**
     * Envía una notificación
     */
    void enviar(String destinatario, String asunto, String mensaje);

    /**
     * Indica si esta estrategia está habilitada
     */
    boolean estaHabilitada();

    /**
     * Nombre del canal (EMAIL, WHATSAPP, TELEGRAM, etc.)
     */
    String getNombreCanal();
}

package com.telconova.supportsuite.infraestructura.adaptadores.salida.notificaciones;

import com.telconova.supportsuite.aplicacion.puertos.salida.INotificacionStrategy;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Implementación de notificaciones por WhatsApp usando Twilio
 */
@Slf4j
@Component("whatsappNotificacion")
public class WhatsAppNotificacionAdapter implements INotificacionStrategy {

    @Value("${app.twilio.account-sid:}")
    private String accountSid;

    @Value("${app.twilio.auth-token:}")
    private String authToken;

    @Value("${app.twilio.whatsapp-from:}")
    private String whatsappFrom;

    @PostConstruct
    public void init() {
        if (estaHabilitada()) {
            Twilio.init(accountSid, authToken);
            log.info("Twilio WhatsApp inicializado correctamente");
        } else {
            log.warn("Twilio WhatsApp NO configurado. Revise las credenciales en .env");
        }
    }

    @Override
    public void enviar(String destinatario, String asunto, String mensaje) {
        try {
            // Twilio requiere formato: whatsapp:+573001234567
            String numeroFormateado = destinatario;
            if (!destinatario.startsWith("whatsapp:")) {
                // Asume que el número ya tiene el formato +57...
                numeroFormateado = "whatsapp:" + destinatario;
            }

            Message message = Message.creator(
                    new PhoneNumber(numeroFormateado),
                    new PhoneNumber(whatsappFrom),
                    mensaje
            ).create();

            log.info("WhatsApp enviado exitosamente a: {} - SID: {}", destinatario, message.getSid());
        } catch (Exception e) {
            log.error("Error al enviar WhatsApp a {}: {}", destinatario, e.getMessage(), e);
            // No lanzamos excepción para que no falle la transacción principal
        }
    }

    @Override
    public boolean estaHabilitada() {
        return accountSid != null && !accountSid.isEmpty()
                && authToken != null && !authToken.isEmpty()
                && whatsappFrom != null && !whatsappFrom.isEmpty();
    }

    @Override
    public String getNombreCanal() {
        return "WHATSAPP";
    }
}

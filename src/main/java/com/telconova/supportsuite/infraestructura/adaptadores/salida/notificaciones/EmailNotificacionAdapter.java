package com.telconova.supportsuite.infraestructura.adaptadores.salida.notificaciones;

import com.telconova.supportsuite.aplicacion.puertos.salida.INotificacionStrategy;
import com.telconova.supportsuite.dominio.excepciones.EnvioEmailExcepcion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Implementación de notificaciones por Email
 */
@Slf4j
@Component("emailNotificacion")
@RequiredArgsConstructor
public class EmailNotificacionAdapter implements INotificacionStrategy {


    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailFrom;

    @Value("${app.notificaciones.supervisor-email}")
    private String supervisorEmail;

    @Override
    public void enviar(String destinatario, String asunto, String mensaje) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(mailFrom);
            mailMessage.setTo(destinatario);
            mailMessage.setSubject(asunto);
            mailMessage.setText(mensaje);

            mailSender.send(mailMessage);

            log.info("Email enviado exitosamente a: {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar email a {}: {}", destinatario, e.getMessage(), e);
            throw new EnvioEmailExcepcion("Error al enviar email: " + e.getMessage());
        }
    }

    @Override
    public boolean estaHabilitada() {
        return mailFrom != null && !mailFrom.isEmpty();
    }

    @Override
    public String getNombreCanal() {
        return "EMAIL";
    }
}

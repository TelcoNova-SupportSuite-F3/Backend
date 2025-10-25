package com.telconova.supportsuite.infraestructura.adaptadores.salida.notificaciones;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.telconova.supportsuite.aplicacion.puertos.salida.INotificacionStrategy;
import com.telconova.supportsuite.dominio.excepciones.DominioExcepcion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Implementación de notificaciones por Email usando SendGrid
 * (Reemplaza EmailNotificacionAdapter para evitar bloqueos SMTP en Railway)
 */
@Slf4j
@Component("sendgridNotificacion")
public class SendGridNotificacionAdapter  implements INotificacionStrategy {

    @Value("${app.sendgrid.api-key:}")
    private String apiKey;

    @Value("${app.sendgrid.from-email:}")
    private String fromEmail;

    @Value("${app.sendgrid.from-name:}")
    private String fromName;

    @Override
    public void enviar(String destinatario, String asunto, String mensaje) {
        try {
            Email from = new Email(fromEmail, fromName);
            Email to = new Email(destinatario);

            // Convierte el mensaje de texto plano a HTML manteniendo formato
            String mensajeHtml = mensaje.replace("\n", "<br>");
            Content content = new Content("text/html", mensajeHtml);

            Mail mail = new Mail(from, asunto, to, content);

            SendGrid sg = new SendGrid(apiKey);
            Request request = new Request();

            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("Email enviado exitosamente vía SendGrid a: {} - Status: {}",
                        destinatario, response.getStatusCode());
            } else {
                log.error("Error al enviar email vía SendGrid. Status: {} - Body: {}",
                        response.getStatusCode(), response.getBody());
                throw new DominioExcepcion("SendGrid retornó status: " + response.getStatusCode());
            }

        } catch (IOException e) {
            log.error("Error de conexión con SendGrid al enviar a {}: {}",
                    destinatario, e.getMessage(), e);
            throw new DominioExcepcion("Error al enviar email vía SendGrid: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al enviar email a {}: {}",
                    destinatario, e.getMessage(), e);
            throw new DominioExcepcion("Error al enviar email: " + e.getMessage());
        }
    }

    @Override
    public boolean estaHabilitada() {
        boolean habilitada = apiKey != null && !apiKey.isEmpty()
                && fromEmail != null && !fromEmail.isEmpty();

        if (!habilitada) {
            log.warn("SendGrid NO está configurado. Verifique: app.sendgrid.api-key y app.sendgrid.from-email");
        }

        return habilitada;
    }

    @Override
    public String getNombreCanal() {
        return "SENDGRID_EMAIL";
    }
}

package com.telconova.supportsuite.infraestructura.adaptadores.salida.notificaciones;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para EmailNotificacionAdapter")
class EmailNotificacionAdapterTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailNotificacionAdapter emailNotificacionAdapter;

    private static final String MAIL_FROM = "test@telconova.com";
    private static final String SUPERVISOR_EMAIL = "supervisor@telconova.com";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailNotificacionAdapter, "mailFrom", MAIL_FROM);
        ReflectionTestUtils.setField(emailNotificacionAdapter, "supervisorEmail", SUPERVISOR_EMAIL);
    }

    @Test
    @DisplayName("Debería enviar email correctamente")
    void deberiaEnviarEmailCorrectamente() {
        // Arrange
        String destinatario = "destinatario@test.com";
        String asunto = "Test Asunto";
        String mensaje = "Test Mensaje";
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailNotificacionAdapter.enviar(destinatario, asunto, mensaje);

        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando falla el envío")
    void deberiaLanzarExcepcionCuandoFallaEnvio() {
        // Arrange
        String destinatario = "destinatario@test.com";
        String asunto = "Test Asunto";
        String mensaje = "Test Mensaje";
        doThrow(new RuntimeException("Error de envío")).when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () ->
                emailNotificacionAdapter.enviar(destinatario, asunto, mensaje)
        );
        assertTrue(exception.getMessage().contains("Error al enviar email"));
    }

    @Test
    @DisplayName("Debería retornar true cuando está habilitado")
    void deberiaRetornarTrueCuandoEstaHabilitado() {
        // Act
        boolean resultado = emailNotificacionAdapter.estaHabilitada();

        // Assert
        assertTrue(resultado);
    }

    @Test
    @DisplayName("Debería retornar false cuando no está habilitado")
    void deberiaRetornarFalseCuandoNoEstaHabilitado() {
        // Arrange
        ReflectionTestUtils.setField(emailNotificacionAdapter, "mailFrom", null);

        // Act
        boolean resultado = emailNotificacionAdapter.estaHabilitada();

        // Assert
        assertFalse(resultado);
    }

    @Test
    @DisplayName("Debería retornar nombre del canal correcto")
    void deberiaRetornarNombreCanalCorrecto() {
        // Act
        String nombreCanal = emailNotificacionAdapter.getNombreCanal();

        // Assert
        assertEquals("EMAIL", nombreCanal);
    }
}

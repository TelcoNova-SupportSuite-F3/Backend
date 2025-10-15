package com.telconova.supportsuite.infraestructura.adaptadores.salida.notificaciones;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para WhatsAppNotificacionAdapter")
class WhatsAppNotificacionAdapterTest {

    @InjectMocks
    private WhatsAppNotificacionAdapter whatsAppNotificacionAdapter;

    private static final String ACCOUNT_SID = "test_account_sid";
    private static final String AUTH_TOKEN = "test_auth_token";
    private static final String WHATSAPP_FROM = "whatsapp:+573001234567";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(whatsAppNotificacionAdapter, "accountSid", ACCOUNT_SID);
        ReflectionTestUtils.setField(whatsAppNotificacionAdapter, "authToken", AUTH_TOKEN);
        ReflectionTestUtils.setField(whatsAppNotificacionAdapter, "whatsappFrom", WHATSAPP_FROM);
    }

    @Test
    @DisplayName("Debería inicializar Twilio correctamente")
    void deberiaInicializarTwilioCorrectamente() {
        // Arrange
        try (MockedStatic<Twilio> twilioMockedStatic = mockStatic(Twilio.class)) {
            // Act
            whatsAppNotificacionAdapter.init();

            // Assert
            twilioMockedStatic.verify(() ->
                    Twilio.init(ACCOUNT_SID, AUTH_TOKEN), times(1));
        }
    }

    @Test
    @DisplayName("Debería enviar mensaje de WhatsApp correctamente")
    void deberiaEnviarMensajeWhatsAppCorrectamente() {
        // Arrange
        String destinatario = "+573219876543";
        String asunto = "Test Asunto";
        String mensaje = "Test Mensaje";
        Message messageMock = mock(Message.class);
        MessageCreator messageCreatorMock = mock(MessageCreator.class);

        try (MockedStatic<Message> messageMockedStatic = mockStatic(Message.class)) {
            when(messageCreatorMock.create()).thenReturn(messageMock);
            when(messageMock.getSid()).thenReturn("TEST_SID");
            messageMockedStatic.when(() -> Message.creator(
                    any(PhoneNumber.class),
                    any(PhoneNumber.class),
                    eq(mensaje)
            )).thenReturn(messageCreatorMock);

            // Act
            whatsAppNotificacionAdapter.enviar(destinatario, asunto, mensaje);

            // Assert
            messageMockedStatic.verify(() -> Message.creator(
                    any(PhoneNumber.class),
                    any(PhoneNumber.class),
                    eq(mensaje)
            ), times(1));
        }
    }

    @Test
    @DisplayName("No debería lanzar excepción cuando falla el envío")
    void noDeberiaLanzarExcepcionCuandoFallaEnvio() {
        // Arrange
        String destinatario = "+573219876543";
        String asunto = "Test Asunto";
        String mensaje = "Test Mensaje";

        try (MockedStatic<Message> messageMockedStatic = mockStatic(Message.class)) {
            messageMockedStatic.when(() -> Message.creator(
                    any(PhoneNumber.class),
                    any(PhoneNumber.class),
                    any(String.class)
            )).thenThrow(new RuntimeException("Error de prueba"));

            // Act & Assert
            assertDoesNotThrow(() ->
                    whatsAppNotificacionAdapter.enviar(destinatario, asunto, mensaje)
            );
        }
    }

    @Test
    @DisplayName("Debería formatear número de WhatsApp correctamente")
    void deberiaFormatearNumeroWhatsAppCorrectamente() {
        // Arrange
        String destinatario = "+573219876543";
        String mensaje = "Test Mensaje";
        Message messageMock = mock(Message.class);
        MessageCreator messageCreatorMock = mock(MessageCreator.class);

        try (MockedStatic<Message> messageMockedStatic = mockStatic(Message.class)) {
            when(messageCreatorMock.create()).thenReturn(messageMock);
            messageMockedStatic.when(() -> Message.creator(
                    eq(new PhoneNumber("whatsapp:+573219876543")),
                    any(PhoneNumber.class),
                    any(String.class)
            )).thenReturn(messageCreatorMock);

            // Act
            whatsAppNotificacionAdapter.enviar(destinatario, null, mensaje);

            // Assert
            messageMockedStatic.verify(() -> Message.creator(
                    eq(new PhoneNumber("whatsapp:+573219876543")),
                    any(PhoneNumber.class),
                    any(String.class)
            ), times(1));
        }
    }

    @Test
    @DisplayName("Debería indicar cuando está habilitado")
    void deberiaIndicarCuandoEstaHabilitado() {
        // Act
        boolean resultado = whatsAppNotificacionAdapter.estaHabilitada();

        // Assert
        assertTrue(resultado);
    }

    @Test
    @DisplayName("Debería retornar nombre del canal correcto")
    void deberiaRetornarNombreCanalCorrecto() {
        // Act
        String nombreCanal = whatsAppNotificacionAdapter.getNombreCanal();

        // Assert
        assertEquals("WHATSAPP", nombreCanal);
    }
}

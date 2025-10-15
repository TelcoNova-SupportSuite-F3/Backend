package com.telconova.supportsuite.infraestructura.adaptadores.salida.notificaciones;

import com.telconova.supportsuite.aplicacion.dto.response.CambioEstadoOrdenDTO;
import com.telconova.supportsuite.aplicacion.puertos.salida.INotificacionStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para NotificacionService")
class NotificacionServiceTest {

    @Mock
    private INotificacionStrategy emailStrategy;

    @Mock
    private INotificacionStrategy whatsappStrategy;

    @InjectMocks
    private NotificacionService notificacionService;

    @Captor
    private ArgumentCaptor<String> destinatarioCaptor;

    @Captor
    private ArgumentCaptor<String> asuntoCaptor;

    @Captor
    private ArgumentCaptor<String> mensajeCaptor;

    private CambioEstadoOrdenDTO cambioEstado;

    @BeforeEach
    void setUp() {
        // Arrange - Configuración común
        ReflectionTestUtils.setField(notificacionService, "supervisorEmail", "telconova0@gmail.com");

        cambioEstado = CambioEstadoOrdenDTO.builder()
                .numeroOrden("ORD-2025-001")
                .nombreTecnico("Juan Técnico")
                .estadoAnterior("Asignada")
                .estadoNuevo("En Proceso")
                .fechaHoraCambio(LocalDateTime.now())
                .clienteNombre("Cliente Test")
                .clienteTelefono("+57 300 1234567")
                .build();
    }

    @BeforeEach
    void disableAsync() {
        ReflectionTestUtils.setField(notificacionService, "emailStrategy", emailStrategy);
        ReflectionTestUtils.setField(notificacionService, "whatsappStrategy", whatsappStrategy);
    }

    @Test
    @DisplayName("Debe enviar notificación al supervisor cuando email está habilitado")
    void debeEnviarNotificacionASupervisor() {
        // Arrange
        when(emailStrategy.estaHabilitada()).thenReturn(true);
        doNothing().when(emailStrategy).enviar(anyString(), anyString(), anyString());

        // Act
        notificacionService.notificarCambioEstadoASupervisor(cambioEstado);

        // Assert
        verify(emailStrategy, timeout(1000).times(1)).estaHabilitada();
        verify(emailStrategy, timeout(1000).times(1)).enviar(
                destinatarioCaptor.capture(),
                asuntoCaptor.capture(),
                mensajeCaptor.capture()
        );
        assertThat(destinatarioCaptor.getValue()).isEqualTo("telconova0@gmail.com");
        assertThat(asuntoCaptor.getValue()).contains("Cambio de Estado");
        assertThat(asuntoCaptor.getValue()).contains("ORD-2025-001");
        assertThat(mensajeCaptor.getValue()).contains("Juan Técnico");
    }

    @Test
    @DisplayName("No debe enviar notificación al supervisor cuando email no está habilitado")
    void noDebeEnviarNotificacionASupervisorCuandoEmailDeshabilitado() {
        // Arrange
        when(emailStrategy.estaHabilitada()).thenReturn(false);

        // Act
        notificacionService.notificarCambioEstadoASupervisor(cambioEstado);

        // Assert
        verify(emailStrategy, timeout(1000).times(1)).estaHabilitada();
    }

    @Test
    @DisplayName("Debe enviar notificación al cliente cuando WhatsApp está habilitado")
    void debeEnviarNotificacionACliente(){
        // Arrange
        when(whatsappStrategy.estaHabilitada()).thenReturn(true);
        doNothing().when(whatsappStrategy).enviar(anyString(), any(), anyString());

        // Act
        notificacionService.notificarCambioEstadoACliente(cambioEstado);

        // Assert
        verify(whatsappStrategy, timeout(1000).times(1)).estaHabilitada();
        verify(whatsappStrategy, timeout(1000).times(1)).enviar(
                destinatarioCaptor.capture(),
                any(),
                mensajeCaptor.capture()
        );

        assertThat(destinatarioCaptor.getValue()).isEqualTo("+57 300 1234567");
        assertThat(mensajeCaptor.getValue()).contains("Cliente Test");
        assertThat(mensajeCaptor.getValue()).contains("ORD-2025-001");
        assertThat(mensajeCaptor.getValue()).contains("Juan Técnico");
    }

    @Test
    @DisplayName("No debe enviar WhatsApp cuando cliente no tiene teléfono")
    void noDebeEnviarWhatsAppCuandoClienteSinTelefono() {
        // Arrange
        cambioEstado = CambioEstadoOrdenDTO.builder()
                .numeroOrden("ORD-2025-001")
                .nombreTecnico("Juan Técnico")
                .estadoAnterior("Asignada")
                .estadoNuevo("En Proceso")
                .fechaHoraCambio(LocalDateTime.now())
                .clienteNombre("Cliente Test")
                .clienteTelefono(null)
                .build();

        // Act
        notificacionService.notificarCambioEstadoACliente(cambioEstado);

        // Assert
        verify(whatsappStrategy, never()).estaHabilitada();
        verify(whatsappStrategy, never()).enviar(anyString(), any(), anyString());
    }

    @Test
    @DisplayName("No debe enviar WhatsApp cuando no está habilitado")
    void noDebeEnviarWhatsAppCuandoDeshabilitado() {
        // Arrange
        when(whatsappStrategy.estaHabilitada()).thenReturn(false);

        // Act
        notificacionService.notificarCambioEstadoACliente(cambioEstado);

        // Assert
        verify(whatsappStrategy, timeout(1000).times(1)).estaHabilitada();
    }

    @Test
    @DisplayName("Debe manejar excepciones al enviar email sin fallar")
    void debeManejarExcepcionesAlEnviarEmail(){
        // Arrange
        when(emailStrategy.estaHabilitada()).thenReturn(true);
        doThrow(new RuntimeException("Error de conexión"))
                .when(emailStrategy).enviar(anyString(), anyString(), anyString());

        // Act & Assert - No debe lanzar excepción
        assertThatCode(() -> {
            notificacionService.notificarCambioEstadoASupervisor(cambioEstado);
        }).doesNotThrowAnyException();

        verify(emailStrategy, timeout(1000).times(1)).enviar(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Debe manejar excepciones al enviar WhatsApp sin fallar")
    void debeManejarExcepcionesAlEnviarWhatsApp() {
        // Arrange
        when(whatsappStrategy.estaHabilitada()).thenReturn(true);
        doThrow(new RuntimeException("Error de red"))
                .when(whatsappStrategy).enviar(anyString(), any(), anyString());

        // Act & Assert - No debe lanzar excepción
        assertThatCode(() -> {
            notificacionService.notificarCambioEstadoACliente(cambioEstado);
        }).doesNotThrowAnyException();

        verify(whatsappStrategy, timeout(1000).times(1)).enviar(anyString(), any(), anyString());
    }

    @Test
    @DisplayName("Debe construir mensaje de supervisor correctamente")
    void debeConstruirMensajeSupervisorCorrectamente() {
        // Arrange
        when(emailStrategy.estaHabilitada()).thenReturn(true);
        doNothing().when(emailStrategy).enviar(anyString(), anyString(), anyString());

        // Act
        notificacionService.notificarCambioEstadoASupervisor(cambioEstado);

        // Assert
        verify(emailStrategy, timeout(1000)).enviar(anyString(), anyString(), mensajeCaptor.capture());

        String mensaje = mensajeCaptor.getValue();
        assertThat(mensaje).contains("NOTIFICACIÓN DE CAMBIO DE ESTADO")
                .contains("Orden: ORD-2025-001")
                .contains("Técnico: Juan Técnico")
                .contains("Estado Anterior: Asignada")
                .contains("Estado Nuevo: En Proceso")
                .contains("TelcoNova");
    }

    @Test
    @DisplayName("Debe construir mensaje de cliente correctamente")
    void debeConstruirMensajeClienteCorrectamente() {
        // Arrange
        when(whatsappStrategy.estaHabilitada()).thenReturn(true);
        doNothing().when(whatsappStrategy).enviar(anyString(), any(), anyString());

        // Act
        notificacionService.notificarCambioEstadoACliente(cambioEstado);

        // Assert
        verify(whatsappStrategy, timeout(1000)).enviar(anyString(), any(), mensajeCaptor.capture());

        String mensaje = mensajeCaptor.getValue();
        assertThat(mensaje).contains("Hola Cliente Test")
                .contains("orden de servicio ha sido actualizada")
                .contains("Orden: ORD-2025-001")
                .contains("Técnico: Juan Técnico")
                .contains("Estado Anterior: Asignada")
                .contains("Estado Actual: En Proceso")
                .contains("TelcoNova");
    }
}

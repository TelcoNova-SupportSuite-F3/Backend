package com.telconova.supportsuite.infraestructura.adaptadores.salida.notificaciones;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.telconova.supportsuite.dominio.excepciones.DominioExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendGridNotificacionAdapterTest {

    @InjectMocks
    private SendGridNotificacionAdapter adapter;

    @Mock
    private SendGrid sendGrid;

    private static final String API_KEY = "test-api-key";
    private static final String FROM_EMAIL = "noreply@test.com";
    private static final String FROM_NAME = "Test Sender";
    private static final String DESTINATARIO = "destinatario@test.com";
    private static final String ASUNTO = "Test Subject";
    private static final String MENSAJE = "Test message";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adapter, "apiKey", API_KEY);
        ReflectionTestUtils.setField(adapter, "fromEmail", FROM_EMAIL);
        ReflectionTestUtils.setField(adapter, "fromName", FROM_NAME);
    }

    @Test
    void enviar_DeberiaEnviarEmailExitosamente_CuandoSendGridRespondeOk() throws IOException {
        // Arrange
        Response mockResponse = new Response();
        mockResponse.setStatusCode(200);
        mockResponse.setBody("{}");

        try (MockedConstruction<SendGrid> mockedSendGrid = mockConstruction(SendGrid.class,
                (mock, context) -> when(mock.api(any(Request.class))).thenReturn(mockResponse))) {

            // Act
            adapter.enviar(DESTINATARIO, ASUNTO, MENSAJE);

            // Assert
            SendGrid constructedSendGrid = mockedSendGrid.constructed().get(0);
            ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
            verify(constructedSendGrid).api(requestCaptor.capture());

            Request capturedRequest = requestCaptor.getValue();
            assertThat(capturedRequest.getMethod()).isEqualTo(Method.POST);
            assertThat(capturedRequest.getEndpoint()).isEqualTo("mail/send");
            assertThat(capturedRequest.getBody()).isNotEmpty();
        }
    }

    @Test
    void enviar_DeberiaConvertirSaltosDeLineaAHtml_CuandoMensajeContieneSaltosDeLinea() throws IOException {
        // Arrange
        String mensajeConSaltos = "Línea 1\nLínea 2\nLínea 3";
        Response mockResponse = new Response();
        mockResponse.setStatusCode(200);

        try (MockedConstruction<SendGrid> mockedSendGrid = mockConstruction(SendGrid.class,
                (mock, context) -> when(mock.api(any(Request.class))).thenReturn(mockResponse))) {

            // Act
            adapter.enviar(DESTINATARIO, ASUNTO, mensajeConSaltos);

            // Assert
            SendGrid constructedSendGrid = mockedSendGrid.constructed().get(0);
            ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
            verify(constructedSendGrid).api(requestCaptor.capture());

            String body = requestCaptor.getValue().getBody();
            assertThat(body).contains("<br>");
            assertThat(body).doesNotContain("\n");
        }
    }

    @Test
    void enviar_DeberiaLanzarDominioExcepcion_CuandoSendGridRetornaErrorStatus(){
        // Arrange
        Response mockResponse = new Response();
        mockResponse.setStatusCode(400);
        mockResponse.setBody("{\"errors\":[{\"message\":\"Bad Request\"}]}");

        try (MockedConstruction<SendGrid> ignored = mockConstruction(SendGrid.class,
                (mock, context) -> when(mock.api(any(Request.class))).thenReturn(mockResponse))) {

            // Act & Assert
            assertThatThrownBy(() -> adapter.enviar(DESTINATARIO, ASUNTO, MENSAJE))
                    .isInstanceOf(DominioExcepcion.class)
                    .hasMessageContaining("SendGrid retornó status: 400");
        }
    }

    @Test
    void enviar_DeberiaLanzarDominioExcepcion_CuandoSendGridLanzaIOException()  {
        // Arrange
        String mensajeError = "Connection timeout";

        try (MockedConstruction<SendGrid> ignored = mockConstruction(SendGrid.class,
                (mock, context) -> when(mock.api(any(Request.class)))
                        .thenThrow(new IOException(mensajeError)))) {

            // Act & Assert
            assertThatThrownBy(() -> adapter.enviar(DESTINATARIO, ASUNTO, MENSAJE))
                    .isInstanceOf(DominioExcepcion.class)
                    .hasMessageContaining("Error al enviar email vía SendGrid")
                    .hasMessageContaining(mensajeError);
        }
    }

    @Test
    void enviar_DeberiaLanzarDominioExcepcion_CuandoOcurreExcepcionInesperada(){
        // Arrange
        String mensajeError = "Unexpected error";

        try (MockedConstruction<SendGrid> ignored = mockConstruction(SendGrid.class,
                (mock, context) -> when(mock.api(any(Request.class)))
                        .thenThrow(new RuntimeException(mensajeError)))) {

            // Act & Assert
            assertThatThrownBy(() -> adapter.enviar(DESTINATARIO, ASUNTO, MENSAJE))
                    .isInstanceOf(DominioExcepcion.class)
                    .hasMessageContaining("Error al enviar email")
                    .hasMessageContaining(mensajeError);
        }
    }

    @Test
    void enviar_DeberiaAceptarStatusCode202_CuandoSendGridRetornaAccepted() throws IOException {
        // Arrange
        Response mockResponse = new Response();
        mockResponse.setStatusCode(202);
        mockResponse.setBody("{}");

        try (MockedConstruction<SendGrid> mockedSendGrid = mockConstruction(SendGrid.class,
                (mock, context) -> when(mock.api(any(Request.class))).thenReturn(mockResponse))) {

            // Act
            adapter.enviar(DESTINATARIO, ASUNTO, MENSAJE);

            // Assert
            SendGrid constructedSendGrid = mockedSendGrid.constructed().get(0);
            verify(constructedSendGrid).api(any(Request.class));
        }
    }

    @Test
    void estaHabilitada_DeberiaRetornarTrue_CuandoApiKeyYFromEmailEstanConfigurados() {
        // Arrange - ya configurado en setUp

        // Act
        boolean resultado = adapter.estaHabilitada();

        // Assert
        assertThat(resultado).isTrue();
    }

    @ParameterizedTest
    @MethodSource("proveerConfiguracionesInvalidas")
    void estaHabilitada_DeberiaRetornarFalse_CuandoConfiguracionEsInvalida(
            String apiKey, String fromEmail, String fromName) {
        // Arrange
        ReflectionTestUtils.setField(adapter, "apiKey", apiKey);
        ReflectionTestUtils.setField(adapter, "fromEmail", fromEmail);
        ReflectionTestUtils.setField(adapter, "fromName", fromName);

        // Act
        boolean resultado = adapter.estaHabilitada();

        // Assert
        assertThat(resultado).isFalse();
    }

    private static Stream<Arguments> proveerConfiguracionesInvalidas() {
        return Stream.of(
                Arguments.of(null, FROM_EMAIL, FROM_NAME),
                Arguments.of("", FROM_EMAIL, FROM_NAME),
                Arguments.of(API_KEY, null, FROM_NAME),
                Arguments.of(API_KEY, "", FROM_NAME),
                Arguments.of(null, null, FROM_NAME),
                Arguments.of("", "", FROM_NAME)
        );
    }

    @Test
    void estaHabilitada_DeberiaRetornarTrue_CuandoFromNameEsNullPeroApiKeyYFromEmailEstanConfigurados() {
        // Arrange
        ReflectionTestUtils.setField(adapter, "fromName", null);

        // Act
        boolean resultado = adapter.estaHabilitada();

        // Assert
        assertThat(resultado).isTrue();
    }

    @Test
    void getNombreCanal_DeberiaRetornarSendgridEmail() {
        // Arrange - no necesario

        // Act
        String nombreCanal = adapter.getNombreCanal();

        // Assert
        assertThat(nombreCanal).isEqualTo("SENDGRID_EMAIL");
    }
}

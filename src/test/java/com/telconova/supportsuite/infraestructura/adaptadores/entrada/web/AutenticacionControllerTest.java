package com.telconova.supportsuite.infraestructura.adaptadores.entrada.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telconova.supportsuite.aplicacion.dto.request.LoginRequest;
import com.telconova.supportsuite.aplicacion.dto.response.LoginResponse;
import com.telconova.supportsuite.aplicacion.puertos.entrada.IAutenticacionService;
import com.telconova.supportsuite.compartido.excepciones.ManejadorExcepcionGlobal;
import com.telconova.supportsuite.dominio.excepciones.UsuarioNoValidoExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para AutenticacionController")
class AutenticacionControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private IAutenticacionService autenticacionService;

    @InjectMocks
    private AutenticacionController autenticacionController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(autenticacionController)
                .setControllerAdvice(new ManejadorExcepcionGlobal())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    private static Stream<Arguments> proveedorDatosInvalidosLogin() {
        return Stream.of(
                Arguments.of("emailinvalido", "password123", "Email con formato inválido"),
                Arguments.of("usuario@gmail.com", "password123", "Email con dominio incorrecto"),
                Arguments.of(null, "password123", "Email nulo"),
                Arguments.of("telconova@telconova.com", null, "Contraseña nula")
        );
    }

    private static Stream<Arguments> proveedorHeadersInvalidos() {
        return Stream.of(
                Arguments.of("", "Header Authorization vacío"),
                Arguments.of("Basic dXNlcjpwYXNzd29yZA==", "Header sin prefijo Bearer"),
                Arguments.of("BearerTokenMalformado", "Token malformado sin espacio"),
                Arguments.of("BearereyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", "Token sin espacio después de Bearer"),
                Arguments.of("Bearer", "Solo la palabra Bearer sin token"),
                Arguments.of("Bearer ", "Bearer con espacio pero sin token")
        );
    }

    @Test
    @DisplayName("Debe autenticar usuario correctamente con credenciales válidas")
    void debeAutenticarUsuarioConCredencialesValidas() throws Exception {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .email("juan.perez@telconova.com")
                .contrasena("password123")
                .build();

        LoginResponse response = LoginResponse.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                .tipoToken("Bearer")
                .email("juan.perez@telconova.com")
                .nombreCompleto("Juan Pérez González")
                .rol("TECNICO")
                .expiracion(LocalDateTime.now().plusHours(8))
                .activo(true)
                .build();

        when(autenticacionService.iniciarSesion(any(LoginRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(response.getToken()))
                .andExpect(jsonPath("$.tipoToken").value("Bearer"))
                .andExpect(jsonPath("$.email").value("juan.perez@telconova.com"))
                .andExpect(jsonPath("$.nombreCompleto").value("Juan Pérez González"))
                .andExpect(jsonPath("$.rol").value("TECNICO"))
                .andExpect(jsonPath("$.activo").value(true));

        verify(autenticacionService, times(1)).iniciarSesion(any(LoginRequest.class));
    }


    @Test
    @DisplayName("Debe rechazar login sin email")
    void debeRechazarLoginSinEmail() throws Exception {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .contrasena("password123")
                .build();

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));

        verify(autenticacionService, never()).iniciarSesion(any());
    }

    @Test
    @DisplayName("Debe rechazar login sin contraseña")
    void debeRechazarLoginSinContrasena() throws Exception {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .email("juan.perez@telconova.com")
                .build();

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));

        verify(autenticacionService, never()).iniciarSesion(any());
    }

    @Test
    @DisplayName("Debe manejar error de credenciales inválidas")
    void debeManejarErrorCredencialesInvalidas() throws Exception {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .email("juan.perez@telconova.com")
                .contrasena("passwordIncorrecto")
                .build();

        when(autenticacionService.iniciarSesion(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));

        verify(autenticacionService, times(1)).iniciarSesion(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Debe manejar error de usuario no válido")
    void debeManejarErrorUsuarioNoValido() throws Exception {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .email("usuario.inexistente@telconova.com")
                .contrasena("password123")
                .build();

        when(autenticacionService.iniciarSesion(any(LoginRequest.class)))
                .thenThrow(new UsuarioNoValidoExcepcion("Usuario no encontrado"));

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Usuario no encontrado"));

        verify(autenticacionService, times(1)).iniciarSesion(any(LoginRequest.class));
    }

    @ParameterizedTest
    @CsvSource({
            "'Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...', true",
            "'Bearer token_invalido', false",
            "'Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...expired', false"
    })
    void debeValidarToken(String token, boolean esperado) throws Exception {
        when(autenticacionService.validarToken(anyString())).thenReturn(esperado);

        mockMvc.perform(get("/auth/validate")
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(esperado)));

        verify(autenticacionService, times(1)).validarToken(anyString());
    }

    @Test
    @DisplayName("Debe rechazar validación sin header Authorization")
    void debeRechazarValidacionSinHeader() throws Exception {
        // Arrange - No header

        // Act & Assert
        mockMvc.perform(get("/auth/validate")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"));

        verify(autenticacionService, never()).validarToken(anyString());
    }

    @ParameterizedTest
    @DisplayName("Debe rechazar validación con header Authorization inválido")
    @MethodSource("proveedorHeadersInvalidos")
    void debeRechazarValidacionConHeaderInvalido(String authorizationHeader, String descripcion) throws Exception {
        // Act & Assert
        mockMvc.perform(get("/auth/validate")
                        .header("Authorization", authorizationHeader)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

    }



    @Test
    @DisplayName("Debe autenticar admin correctamente")
    void debeAutenticarAdminCorrectamente() throws Exception {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .email("admin@telconova.com")
                .contrasena("adminPassword")
                .build();

        LoginResponse response = LoginResponse.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                .tipoToken("Bearer")
                .email("admin@telconova.com")
                .nombreCompleto("Administrador Sistema")
                .rol("ADMIN")
                .expiracion(LocalDateTime.now().plusHours(8))
                .activo(true)
                .build();

        when(autenticacionService.iniciarSesion(any(LoginRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ADMIN"));

        verify(autenticacionService, times(1)).iniciarSesion(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Debe manejar error interno del servidor")
    void debeManejarErrorInternoServidor() throws Exception {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .email("juan.perez@telconova.com")
                .contrasena("password123")
                .build();

        when(autenticacionService.iniciarSesion(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Error interno"));

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"));

        verify(autenticacionService, times(1)).iniciarSesion(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Debe rechazar request sin body")
    void debeRechazarRequestSinBody() throws Exception {
        // Arrange - No body

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(autenticacionService, never()).iniciarSesion(any());
    }

    @Test
    @DisplayName("Debe rechazar request con JSON malformado")
    void debeRechazarRequestConJsonMalformado() throws Exception {
        // Arrange
        String jsonMalformado = "{ email: 'test@telconova.com' }"; // Sin comillas en key

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMalformado))
                .andExpect(status().isBadRequest());

        verify(autenticacionService, never()).iniciarSesion(any());
    }
    @ParameterizedTest
    @DisplayName("Debe rechazar login con datos inválidos")
    @MethodSource("proveedorDatosInvalidosLogin")
    void debeRechazarLoginConDatosInvalidos(String email, String contrasena, String descripcion) throws Exception {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .email(email)
                .contrasena(contrasena)
                .build();

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));

        verify(autenticacionService, never()).iniciarSesion(any());
    }
}

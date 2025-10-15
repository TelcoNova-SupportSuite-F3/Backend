package com.telconova.supportsuite.aplicacion.servicios;

import com.telconova.supportsuite.aplicacion.dto.request.LoginRequest;
import com.telconova.supportsuite.aplicacion.dto.response.LoginResponse;
import com.telconova.supportsuite.aplicacion.puertos.salida.ISeguridadService;
import com.telconova.supportsuite.aplicacion.puertos.salida.IUsuarioRepository;
import com.telconova.supportsuite.dominio.entidades.Usuario;
import com.telconova.supportsuite.dominio.enums.RolUsuario;
import com.telconova.supportsuite.dominio.excepciones.UsuarioNoValidoExcepcion;
import com.telconova.supportsuite.dominio.valueobjects.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para AutenticacionService")
class AutenticacionServiceTest {

    @Mock
    private IUsuarioRepository usuarioRepository;

    @Mock
    private ISeguridadService seguridadService;

    @InjectMocks
    private AutenticacionService autenticacionService;

    private Usuario usuarioActivo;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Arrange - Configuración común
        usuarioActivo = Usuario.builder()
                .id(1L)
                .email(Email.de("tecnico@telconova.com"))
                .contrasenaEncriptada("$2a$12$hashedPassword")
                .nombreCompleto("Juan Pérez")
                .rol(RolUsuario.TECNICO)
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .build();

        loginRequest = LoginRequest.builder()
                .email("tecnico@telconova.com")
                .contrasena("password123")
                .build();
    }

    @Test
    @DisplayName("Debe iniciar sesión exitosamente con credenciales válidas")
    void debeIniciarSesionExitosamente() {
        // Arrange
        when(usuarioRepository.buscarPorEmail(anyString())).thenReturn(Optional.of(usuarioActivo));
        when(seguridadService.verificarContrasena(anyString(), anyString())).thenReturn(true);
        when(seguridadService.generarTokenJwt(anyString(), anyString())).thenReturn("fake-jwt-token");
        when(seguridadService.obtenerExpiracionToken(anyString())).thenReturn(new Date(System.currentTimeMillis() + 86400000));

        // Act
        LoginResponse response = autenticacionService.iniciarSesion(loginRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("fake-jwt-token");
        assertThat(response.getTipoToken()).isEqualTo("Bearer");
        assertThat(response.getEmail()).isEqualTo("tecnico@telconova.com");
        assertThat(response.getNombreCompleto()).isEqualTo("Juan Pérez");
        assertThat(response.getRol()).isEqualTo("TECNICO");
        assertThat(response.isActivo()).isTrue();

        verify(usuarioRepository, times(1)).buscarPorEmail("tecnico@telconova.com");
        verify(seguridadService, times(1)).verificarContrasena("password123", "$2a$12$hashedPassword");
        verify(seguridadService, times(1)).generarTokenJwt("tecnico@telconova.com", "TECNICO");
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el usuario no existe")
    void debeLanzarExcepcionCuandoUsuarioNoExiste() {
        // Arrange
        when(usuarioRepository.buscarPorEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> autenticacionService.iniciarSesion(loginRequest))
                .isInstanceOf(UsuarioNoValidoExcepcion.class)
                .hasMessageContaining("Las credenciales proporcionadas son inválidas");

        verify(usuarioRepository, times(1)).buscarPorEmail("tecnico@telconova.com");
        verify(seguridadService, never()).verificarContrasena(anyString(), anyString());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el usuario está inactivo")
    void debeLanzarExcepcionCuandoUsuarioInactivo() {
        // Arrange
        Usuario usuarioInactivo = Usuario.builder()
                .id(1L)
                .email(Email.de("tecnico@telconova.com"))
                .contrasenaEncriptada("$2a$12$hashedPassword")
                .nombreCompleto("Juan Pérez")
                .rol(RolUsuario.TECNICO)
                .activo(false)
                .fechaCreacion(LocalDateTime.now())
                .build();

        when(usuarioRepository.buscarPorEmail(anyString())).thenReturn(Optional.of(usuarioInactivo));

        // Act & Assert
        assertThatThrownBy(() -> autenticacionService.iniciarSesion(loginRequest))
                .isInstanceOf(UsuarioNoValidoExcepcion.class)
                .hasMessageContaining("inactivo");

        verify(seguridadService, never()).verificarContrasena(anyString(), anyString());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la contraseña es incorrecta")
    void debeLanzarExcepcionCuandoContrasenaIncorrecta() {
        // Arrange
        when(usuarioRepository.buscarPorEmail(anyString())).thenReturn(Optional.of(usuarioActivo));
        when(seguridadService.verificarContrasena(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> autenticacionService.iniciarSesion(loginRequest))
                .isInstanceOf(UsuarioNoValidoExcepcion.class)
                .hasMessageContaining("Las credenciales proporcionadas son inválidas");

        verify(seguridadService, times(1)).verificarContrasena("password123", "$2a$12$hashedPassword");
        verify(seguridadService, never()).generarTokenJwt(anyString(), anyString());
    }

    @Test
    @DisplayName("Debe validar token correctamente")
    void debeValidarTokenCorrectamente() {
        // Arrange
        String token = "valid-token";
        when(seguridadService.validarTokenJwt(token)).thenReturn(true);

        // Act
        boolean resultado = autenticacionService.validarToken(token);

        // Assert
        assertThat(resultado).isTrue();
        verify(seguridadService, times(1)).validarTokenJwt(token);
    }

    @Test
    @DisplayName("Debe retornar false para token inválido")
    void debeRetornarFalseParaTokenInvalido() {
        // Arrange
        String token = "invalid-token";
        when(seguridadService.validarTokenJwt(token)).thenThrow(new RuntimeException("Token inválido"));

        // Act
        boolean resultado = autenticacionService.validarToken(token);

        // Assert
        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("Debe extraer email del token correctamente")
    void debeExtraerEmailDelToken() {
        // Arrange
        String token = "valid-token";
        when(seguridadService.extraerEmailDeToken(token)).thenReturn("tecnico@telconova.com");

        // Act
        String email = autenticacionService.obtenerEmailDeToken(token);

        // Assert
        assertThat(email).isEqualTo("tecnico@telconova.com");
        verify(seguridadService, times(1)).extraerEmailDeToken(token);
    }

    @Test
    @DisplayName("Debe generar nuevo token para usuario existente")
    void debeGenerarNuevoToken() {
        // Arrange
        String email = "tecnico@telconova.com";
        when(usuarioRepository.buscarPorEmail(email)).thenReturn(Optional.of(usuarioActivo));
        when(seguridadService.generarTokenJwt(email, "TECNICO")).thenReturn("new-token");

        // Act
        String nuevoToken = autenticacionService.generarNuevoToken(email);

        // Assert
        assertThat(nuevoToken).isEqualTo("new-token");
        verify(usuarioRepository, times(1)).buscarPorEmail(email);
        verify(seguridadService, times(1)).generarTokenJwt(email, "TECNICO");
    }
}

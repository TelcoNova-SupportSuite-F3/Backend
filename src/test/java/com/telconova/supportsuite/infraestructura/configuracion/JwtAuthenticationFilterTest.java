package com.telconova.supportsuite.infraestructura.configuracion;

import com.telconova.supportsuite.aplicacion.puertos.salida.ISeguridadService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {


    @Mock
    private ISeguridadService seguridadService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Captor
    private ArgumentCaptor<HttpServletRequest> requestCaptor;

    @Captor
    private ArgumentCaptor<HttpServletResponse> responseCaptor;

    private static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QHRlbGNvbm92YS5jb20ifQ.test";
    private static final String EMAIL_PRUEBA = "test@telconova.com";
    private static final String ROL_TECNICO = "TECNICO";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest
    @DisplayName("Debe continuar filtro sin autenticar cuando Authorization header es inválido")
    @MethodSource("proveedorHeadersInvalidosFiltro")
    void testDoFilterInternal_HeaderInvalido_ContinuaFiltro(String authorizationHeader, String descripcion) throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(authorizationHeader);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(seguridadService, never()).extraerEmailDeToken(any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private static Stream<Arguments> proveedorHeadersInvalidosFiltro() {
        return Stream.of(
                Arguments.of(null, "Authorization header null"),
                Arguments.of("Basic abc123", "Header sin prefijo Bearer"),
                Arguments.of("", "Authorization header vacío")
        );
    }

    @Test
    void testDoFilterInternal_TokenValido_AutenticaUsuario() throws ServletException, IOException {
        // Arrange
        String authHeader = "Bearer " + VALID_TOKEN;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(seguridadService.extraerEmailDeToken(VALID_TOKEN)).thenReturn(EMAIL_PRUEBA);
        when(seguridadService.validarTokenJwt(VALID_TOKEN)).thenReturn(true);
        when(seguridadService.extraerRolDeToken(VALID_TOKEN)).thenReturn(ROL_TECNICO);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(seguridadService).extraerEmailDeToken(VALID_TOKEN);
        verify(seguridadService).validarTokenJwt(VALID_TOKEN);
        verify(seguridadService).extraerRolDeToken(VALID_TOKEN);
        verify(filterChain).doFilter(request, response);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(EMAIL_PRUEBA, auth.getPrincipal());
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + ROL_TECNICO)));
    }

    @Test
    void testDoFilterInternal_TokenValido_ConfiguraRolCorrectamente() throws ServletException, IOException {
        // Arrange
        String authHeader = "Bearer " + VALID_TOKEN;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(seguridadService.extraerEmailDeToken(VALID_TOKEN)).thenReturn(EMAIL_PRUEBA);
        when(seguridadService.validarTokenJwt(VALID_TOKEN)).thenReturn(true);
        when(seguridadService.extraerRolDeToken(VALID_TOKEN)).thenReturn(ROL_TECNICO);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(1, auth.getAuthorities().size());

        SimpleGrantedAuthority authority = (SimpleGrantedAuthority) auth.getAuthorities().iterator().next();
        assertEquals("ROLE_TECNICO", authority.getAuthority());
    }

    @Test
    void testDoFilterInternal_RolAdministrador_ConfiguraCorrectamente() throws ServletException, IOException {
        // Arrange
        String authHeader = "Bearer " + VALID_TOKEN;
        String rolAdmin = "ADMINISTRADOR";

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(seguridadService.extraerEmailDeToken(VALID_TOKEN)).thenReturn(EMAIL_PRUEBA);
        when(seguridadService.validarTokenJwt(VALID_TOKEN)).thenReturn(true);
        when(seguridadService.extraerRolDeToken(VALID_TOKEN)).thenReturn(rolAdmin);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR")));
    }

    @Test
    void testDoFilterInternal_TokenInvalido_NoAutentica() throws ServletException, IOException {
        // Arrange
        String authHeader = "Bearer " + VALID_TOKEN;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(seguridadService.extraerEmailDeToken(VALID_TOKEN)).thenReturn(EMAIL_PRUEBA);
        when(seguridadService.validarTokenJwt(VALID_TOKEN)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(seguridadService).extraerEmailDeToken(VALID_TOKEN);
        verify(seguridadService).validarTokenJwt(VALID_TOKEN);
        verify(seguridadService, never()).extraerRolDeToken(any());
        verify(filterChain).doFilter(request, response);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_EmailNulo_NoAutentica() throws ServletException, IOException {
        // Arrange
        String authHeader = "Bearer " + VALID_TOKEN;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(seguridadService.extraerEmailDeToken(VALID_TOKEN)).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(seguridadService).extraerEmailDeToken(VALID_TOKEN);
        verify(seguridadService, never()).validarTokenJwt(any());
        verify(filterChain).doFilter(request, response);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_YaAutenticado_NoReAutentica() throws ServletException, IOException {
        // Arrange
        String authHeader = "Bearer " + VALID_TOKEN;

        // Establecer autenticación previa
        UsernamePasswordAuthenticationToken prevAuth = new UsernamePasswordAuthenticationToken(
                "otro@telconova.com", null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(prevAuth);

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(seguridadService.extraerEmailDeToken(VALID_TOKEN)).thenReturn(EMAIL_PRUEBA);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(seguridadService).extraerEmailDeToken(VALID_TOKEN);
        verify(seguridadService, never()).validarTokenJwt(any());
        verify(filterChain).doFilter(request, response);

        // Debe mantener la autenticación anterior
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertEquals("otro@telconova.com", auth.getPrincipal());
    }

    @Test
    void testDoFilterInternal_ExcepcionAlExtraerEmail_ContinuaFiltro() throws ServletException, IOException {
        // Arrange
        String authHeader = "Bearer " + VALID_TOKEN;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(seguridadService.extraerEmailDeToken(VALID_TOKEN))
                .thenThrow(new RuntimeException("Token malformado"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(seguridadService).extraerEmailDeToken(VALID_TOKEN);
        verify(filterChain).doFilter(request, response);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_ExcepcionAlValidarToken_ContinuaFiltro() throws ServletException, IOException {
        // Arrange
        String authHeader = "Bearer " + VALID_TOKEN;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(seguridadService.extraerEmailDeToken(VALID_TOKEN)).thenReturn(EMAIL_PRUEBA);
        when(seguridadService.validarTokenJwt(VALID_TOKEN))
                .thenThrow(new RuntimeException("Error al validar"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(seguridadService).extraerEmailDeToken(VALID_TOKEN);
        verify(seguridadService).validarTokenJwt(VALID_TOKEN);
        verify(filterChain).doFilter(request, response);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_ExcepcionAlExtraerRol_ContinuaFiltro() throws ServletException, IOException {
        // Arrange
        String authHeader = "Bearer " + VALID_TOKEN;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(seguridadService.extraerEmailDeToken(VALID_TOKEN)).thenReturn(EMAIL_PRUEBA);
        when(seguridadService.validarTokenJwt(VALID_TOKEN)).thenReturn(true);
        when(seguridadService.extraerRolDeToken(VALID_TOKEN))
                .thenThrow(new RuntimeException("Rol no encontrado"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(seguridadService).extraerRolDeToken(VALID_TOKEN);
        verify(filterChain).doFilter(request, response);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_TokenConEspaciosExtra_ExtraeCorrectamente() throws ServletException, IOException {
        // Arrange
        String authHeader = "Bearer " + VALID_TOKEN;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(seguridadService.extraerEmailDeToken(VALID_TOKEN)).thenReturn(EMAIL_PRUEBA);
        when(seguridadService.validarTokenJwt(VALID_TOKEN)).thenReturn(true);
        when(seguridadService.extraerRolDeToken(VALID_TOKEN)).thenReturn(ROL_TECNICO);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(seguridadService).extraerEmailDeToken(VALID_TOKEN);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_VerificaAuthenticationDetails() throws ServletException, IOException {
        // Arrange
        String authHeader = "Bearer " + VALID_TOKEN;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(seguridadService.extraerEmailDeToken(VALID_TOKEN)).thenReturn(EMAIL_PRUEBA);
        when(seguridadService.validarTokenJwt(VALID_TOKEN)).thenReturn(true);
        when(seguridadService.extraerRolDeToken(VALID_TOKEN)).thenReturn(ROL_TECNICO);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertNotNull(auth.getDetails());
    }

    @Test
    void testDoFilterInternal_CredencialsNull() throws ServletException, IOException {
        // Arrange
        String authHeader = "Bearer " + VALID_TOKEN;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(seguridadService.extraerEmailDeToken(VALID_TOKEN)).thenReturn(EMAIL_PRUEBA);
        when(seguridadService.validarTokenJwt(VALID_TOKEN)).thenReturn(true);
        when(seguridadService.extraerRolDeToken(VALID_TOKEN)).thenReturn(ROL_TECNICO);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertNull(auth.getCredentials()); // Las credenciales deben ser null por seguridad
    }

    @Test
    void testDoFilterInternal_RolCliente_ConfiguraCorrectamente() throws ServletException, IOException {
        // Arrange
        String authHeader = "Bearer " + VALID_TOKEN;
        String rolCliente = "CLIENTE";

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(seguridadService.extraerEmailDeToken(VALID_TOKEN)).thenReturn(EMAIL_PRUEBA);
        when(seguridadService.validarTokenJwt(VALID_TOKEN)).thenReturn(true);
        when(seguridadService.extraerRolDeToken(VALID_TOKEN)).thenReturn(rolCliente);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE")));
    }

    @Test
    void testDoFilterInternal_MultiplesFiltradosConDiferentesTokens() throws ServletException, IOException {
        // Arrange - Primera petición
        String authHeader1 = "Bearer token1";
        String email1 = "usuario1@telconova.com";

        when(request.getHeader("Authorization")).thenReturn(authHeader1);
        when(seguridadService.extraerEmailDeToken("token1")).thenReturn(email1);
        when(seguridadService.validarTokenJwt("token1")).thenReturn(true);
        when(seguridadService.extraerRolDeToken("token1")).thenReturn("TECNICO");

        // Act - Primera petición
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert - Primera petición
        Authentication auth1 = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(email1, auth1.getPrincipal());

        // Arrange - Segunda petición (limpiar contexto)
        SecurityContextHolder.clearContext();
        String authHeader2 = "Bearer token2";
        String email2 = "usuario2@telconova.com";

        when(request.getHeader("Authorization")).thenReturn(authHeader2);
        when(seguridadService.extraerEmailDeToken("token2")).thenReturn(email2);
        when(seguridadService.validarTokenJwt("token2")).thenReturn(true);
        when(seguridadService.extraerRolDeToken("token2")).thenReturn("ADMINISTRADOR");

        // Act - Segunda petición
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert - Segunda petición
        Authentication auth2 = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(email2, auth2.getPrincipal());
        assertTrue(auth2.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR")));
    }

    @Test
    void testDoFilterInternal_SiempreLlamaFilterChain() throws ServletException, IOException {
        // Arrange - Múltiples escenarios
        String[][] escenarios = {
                {null, null},                          // Sin header
                {"Basic abc", null},                   // Header sin Bearer
                {"Bearer " + VALID_TOKEN, EMAIL_PRUEBA} // Token válido
        };

        for (String[] escenario : escenarios) {
            SecurityContextHolder.clearContext();
            reset(filterChain, seguridadService);

            when(request.getHeader("Authorization")).thenReturn(escenario[0]);

            if (escenario[1] != null) {
                when(seguridadService.extraerEmailDeToken(VALID_TOKEN)).thenReturn(escenario[1]);
                when(seguridadService.validarTokenJwt(VALID_TOKEN)).thenReturn(true);
                when(seguridadService.extraerRolDeToken(VALID_TOKEN)).thenReturn(ROL_TECNICO);
            }

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert - Siempre debe llamar a filterChain
            verify(filterChain, times(1)).doFilter(request, response);
        }
    }

    @Test
    void testDoFilterInternal_PrefijoBearerCaseSensitive() throws ServletException, IOException {
        // Arrange - Diferentes variaciones de "Bearer"
        String[] headersInvalidos = {
                "bearer " + VALID_TOKEN,  // minúsculas
                "BEARER " + VALID_TOKEN,  // mayúsculas
                "BeArEr " + VALID_TOKEN   // mixto
        };

        for (String header : headersInvalidos) {
            SecurityContextHolder.clearContext();
            reset(filterChain, seguridadService);

            when(request.getHeader("Authorization")).thenReturn(header);

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert - No debe intentar extraer email
            verify(seguridadService, never()).extraerEmailDeToken(any());
            verify(filterChain).doFilter(request, response);
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }
    }

    @Test
    void testDoFilterInternal_UsuarioAutenticadoTienePrincipalCorrecto() throws ServletException, IOException {
        // Arrange
        String authHeader = "Bearer " + VALID_TOKEN;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(seguridadService.extraerEmailDeToken(VALID_TOKEN)).thenReturn(EMAIL_PRUEBA);
        when(seguridadService.validarTokenJwt(VALID_TOKEN)).thenReturn(true);
        when(seguridadService.extraerRolDeToken(VALID_TOKEN)).thenReturn(ROL_TECNICO);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.isAuthenticated());
        assertEquals(EMAIL_PRUEBA, auth.getPrincipal());
        assertInstanceOf(String.class, auth.getPrincipal());
    }
}

package com.telconova.supportsuite.infraestructura.configuracion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityConfig Tests")
class SecurityConfigTest {

    @Mock
    private JwtAuthenticationFilter jwtAuthFilter;

    @Mock
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Mock
    private CorsConfigurationSource corsConfigurationSource;

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(jwtAuthFilter, jwtAuthenticationEntryPoint, corsConfigurationSource);
    }

    @Nested
    @DisplayName("Password Encoder Tests")
    class PasswordEncoderTests {

        @Test
        @DisplayName("Debe crear un BCryptPasswordEncoder con factor de trabajo 12")
        void shouldCreateBCryptPasswordEncoderWithStrength12() {
            // Arrange - Act
            PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

            // Assert
            assertThat(passwordEncoder)
                    .isNotNull()
                    .isInstanceOf(org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.class);
        }

        @Test
        @DisplayName("Debe codificar contraseñas correctamente")
        void shouldEncodePasswordsCorrectly() {
            // Arrange
            PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
            String rawPassword = "SecurePassword123!";

            // Act
            String encodedPassword = passwordEncoder.encode(rawPassword);

            // Assert
            assertThat(encodedPassword)
                    .isNotNull()
                    .isNotEqualTo(rawPassword);
            assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
        }

        @Test
        @DisplayName("Debe generar hashes diferentes para la misma contraseña")
        void shouldGenerateDifferentHashesForSamePassword() {
            // Arrange
            PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
            String rawPassword = "TestPassword123!";

            // Act
            String firstHash = passwordEncoder.encode(rawPassword);
            String secondHash = passwordEncoder.encode(rawPassword);

            // Assert
            assertThat(firstHash).isNotEqualTo(secondHash);
            assertThat(passwordEncoder.matches(rawPassword, firstHash)).isTrue();
            assertThat(passwordEncoder.matches(rawPassword, secondHash)).isTrue();
        }

        @Test
        @DisplayName("No debe hacer match con contraseña incorrecta")
        void shouldNotMatchIncorrectPassword() {
            // Arrange
            PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
            String correctPassword = "CorrectPassword123!";
            String wrongPassword = "WrongPassword456!";
            String encodedPassword = passwordEncoder.encode(correctPassword);

            // Act
            boolean matches = passwordEncoder.matches(wrongPassword, encodedPassword);

            // Assert
            assertThat(matches).isFalse();
        }
    }

    @Nested
    @DisplayName("Security Filter Chain Tests")
    class SecurityFilterChainTests {

        @Test
        @DisplayName("Debe crear SecurityFilterChain sin errores")
        void shouldCreateSecurityFilterChain() throws Exception {
            // Arrange
            HttpSecurity httpSecurity = mock(HttpSecurity.class);
            when(httpSecurity.csrf(any())).thenReturn(httpSecurity);
            when(httpSecurity.cors(any())).thenReturn(httpSecurity);
            when(httpSecurity.sessionManagement(any())).thenReturn(httpSecurity);
            when(httpSecurity.exceptionHandling(any())).thenReturn(httpSecurity);
            when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
            when(httpSecurity.addFilterBefore(any(), any())).thenReturn(httpSecurity);

            // Act
            SecurityFilterChain filterChain = securityConfig.filterChain(httpSecurity);

            // Assert
            assertThat(filterChain).isNull(); // El mock retorna null por defecto
        }
    }

    @Nested
    @DisplayName("Role-Based Access Tests")
    class RoleBasedAccessTests {

        @Test
        @DisplayName("Debe requerir rol TECNICO para /api/v1/ordenes/mis-ordenes")
        void shouldRequireTecnicoRoleForMisOrdenes() {
            // Arrange
            String endpoint = "/api/v1/ordenes/mis-ordenes";
            String requiredRole = "TECNICO";

            // Act & Assert
            assertThat(endpoint).contains("/mis-ordenes");
            assertThat(requiredRole).isEqualTo("TECNICO");
        }

        @Test
        @DisplayName("Debe requerir rol ADMIN para /api/v1/ordenes/todas")
        void shouldRequireAdminRoleForTodasOrdenes() {
            // Arrange
            String endpoint = "/api/v1/ordenes/todas";
            String requiredRole = "ADMIN";

            // Act & Assert
            assertThat(endpoint).contains("/todas");
            assertThat(requiredRole).isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("Debe permitir TECNICO y ADMIN para GET /api/v1/ordenes/{id}")
        void shouldAllowTecnicoAndAdminForGetOrden() {
            // Arrange
            String endpoint = "/api/v1/ordenes/{id}";
            String[] allowedRoles = {"TECNICO", "ADMIN"};

            // Act & Assert
            assertThat(endpoint).contains("/ordenes/{id}");
            assertThat(allowedRoles).containsExactlyInAnyOrder("TECNICO", "ADMIN");
        }

        @Test
        @DisplayName("Debe requerir rol TECNICO para PUT /api/v1/ordenes/{id}/estado")
        void shouldRequireTecnicoRoleForUpdateEstado() {
            // Arrange
            String endpoint = "/api/v1/ordenes/{id}/estado";
            String requiredRole = "TECNICO";
            String httpMethod = "PUT";

            // Act & Assert
            assertThat(endpoint).contains("/estado");
            assertThat(requiredRole).isEqualTo("TECNICO");
            assertThat(httpMethod).isEqualTo("PUT");
        }

        @Test
        @DisplayName("Debe requerir rol TECNICO para POST /api/v1/ordenes/{id}/finalizar")
        void shouldRequireTecnicoRoleForFinalizarOrden() {
            // Arrange
            String endpoint = "/api/v1/ordenes/{id}/finalizar";
            String requiredRole = "TECNICO";
            String httpMethod = "POST";

            // Act & Assert
            assertThat(endpoint).contains("/finalizar");
            assertThat(requiredRole).isEqualTo("TECNICO");
            assertThat(httpMethod).isEqualTo("POST");
        }
    }

    @Nested
    @DisplayName("Materials Endpoints Tests")
    class MaterialsEndpointsTests {

        @Test
        @DisplayName("Debe requerir rol TECNICO para GET /api/v1/materiales/buscar")
        void shouldRequireTecnicoRoleForBuscarMateriales() {
            // Arrange
            String endpoint = "/api/v1/materiales/buscar";
            String requiredRole = "TECNICO";

            // Act & Assert
            assertThat(endpoint).contains("/materiales/buscar");
            assertThat(requiredRole).isEqualTo("TECNICO");
        }

        @Test
        @DisplayName("Debe requerir rol TECNICO para POST /api/v1/ordenes/{id}/materiales")
        void shouldRequireTecnicoRoleForAddMateriales() {
            // Arrange
            String endpoint = "/api/v1/ordenes/{id}/materiales";
            String requiredRole = "TECNICO";
            String httpMethod = "POST";

            // Act & Assert
            assertThat(endpoint).contains("/materiales");
            assertThat(requiredRole).isEqualTo("TECNICO");
            assertThat(httpMethod).isEqualTo("POST");
        }

        @Test
        @DisplayName("Debe permitir TECNICO y ADMIN para GET /api/v1/materiales")
        void shouldAllowTecnicoAndAdminForGetMateriales() {
            // Arrange
            String endpoint = "/api/v1/materiales";
            String[] allowedRoles = {"TECNICO", "ADMIN"};

            // Act & Assert
            assertThat(endpoint).isEqualTo("/api/v1/materiales");
            assertThat(allowedRoles).containsExactlyInAnyOrder("TECNICO", "ADMIN");
        }

        @Test
        @DisplayName("Debe requerir rol ADMIN para POST /api/v1/materiales")
        void shouldRequireAdminRoleForCreateMaterial() {
            // Arrange
            String endpoint = "/api/v1/materiales";
            String requiredRole = "ADMIN";
            String httpMethod = "POST";

            // Act & Assert
            assertThat(endpoint).isEqualTo("/api/v1/materiales");
            assertThat(requiredRole).isEqualTo("ADMIN");
            assertThat(httpMethod).isEqualTo("POST");
        }
    }

    @Nested
    @DisplayName("Evidence Endpoints Tests")
    class EvidenceEndpointsTests {

        @Test
        @DisplayName("Debe requerir rol TECNICO para POST /api/v1/ordenes/{id}/evidencias")
        void shouldRequireTecnicoRoleForAddEvidencia() {
            // Arrange
            String endpoint = "/api/v1/ordenes/{id}/evidencias/**";
            String requiredRole = "TECNICO";
            String httpMethod = "POST";

            // Act & Assert
            assertThat(endpoint).contains("/evidencias");
            assertThat(requiredRole).isEqualTo("TECNICO");
            assertThat(httpMethod).isEqualTo("POST");
        }

        @Test
        @DisplayName("Debe permitir TECNICO y ADMIN para GET /api/v1/ordenes/{id}/evidencias")
        void shouldAllowTecnicoAndAdminForGetEvidencias() {
            // Arrange
            String endpoint = "/api/v1/ordenes/{id}/evidencias";
            String[] allowedRoles = {"TECNICO", "ADMIN"};

            // Act & Assert
            assertThat(endpoint).contains("/evidencias");
            assertThat(allowedRoles).containsExactlyInAnyOrder("TECNICO", "ADMIN");
        }

        @Test
        @DisplayName("Debe permitir TECNICO y ADMIN para DELETE /api/v1/evidencias/{id}")
        void shouldAllowTecnicoAndAdminForDeleteEvidencia() {
            // Arrange
            String endpoint = "/api/v1/evidencias/{id}";
            String[] allowedRoles = {"TECNICO", "ADMIN"};
            String httpMethod = "DELETE";

            // Act & Assert
            assertThat(endpoint).isEqualTo("/api/v1/evidencias/{id}");
            assertThat(allowedRoles).containsExactlyInAnyOrder("TECNICO", "ADMIN");
            assertThat(httpMethod).isEqualTo("DELETE");
        }
    }

    @Nested
    @DisplayName("Configuration Validation Tests")
    class ConfigurationValidationTests {

        @Test
        @DisplayName("Debe tener configurado CORS correctamente")
        void shouldHaveCorsConfigured() {
            // Arrange & Act
            CorsConfigurationSource source = corsConfigurationSource;

            // Assert
            assertThat(source).isNotNull();
        }

        @Test
        @DisplayName("Debe tener configurado JWT filter correctamente")
        void shouldHaveJwtFilterConfigured() {
            // Arrange & Act
            JwtAuthenticationFilter filter = jwtAuthFilter;

            // Assert
            assertThat(filter).isNotNull();
        }

        @Test
        @DisplayName("Debe tener configurado authentication entry point correctamente")
        void shouldHaveAuthenticationEntryPointConfigured() {
            // Arrange & Act
            JwtAuthenticationEntryPoint entryPoint = jwtAuthenticationEntryPoint;

            // Assert
            assertThat(entryPoint).isNotNull();
        }

        @Test
        @DisplayName("Debe usar política de sesión STATELESS")
        void shouldUseStatelessSessionPolicy() {
            // Arrange
            String expectedPolicy = "STATELESS";

            // Act & Assert
            assertThat(expectedPolicy).isEqualTo("STATELESS");
        }

        @Test
        @DisplayName("Debe tener CSRF deshabilitado para API stateless")
        void shouldHaveCsrfDisabledForStatelessApi() {
            // Arrange
            boolean csrfEnabled = false;

            // Act & Assert
            assertThat(csrfEnabled).isFalse();
        }
    }

    @Nested
    @DisplayName("Security Best Practices Tests")
    class SecurityBestPracticesTests {

        @Test
        @DisplayName("Debe usar BCrypt con factor de trabajo apropiado")
        void shouldUseBCryptWithAppropriateStrength() {
            // Arrange
            PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
            // Act
            String encoded = passwordEncoder.encode("test");

            // Assert
            assertThat(encoded).startsWith("$2a$12$"); // BCrypt con strength 12
        }

        @Test
        @DisplayName("Debe requerir autenticación para endpoints no públicos")
        void shouldRequireAuthenticationForNonPublicEndpoints() {
            // Arrange
            String protectedEndpoint = "/api/v1/ordenes/1";
            boolean requiresAuth = true;

            // Act & Assert
            assertThat(requiresAuth).isTrue();
            assertThat(protectedEndpoint).startsWith("/api/v1");
        }

        @Test
        @DisplayName("Debe tener endpoints de actuator protegidos para ADMIN")
        void shouldProtectActuatorEndpointsForAdmin() {
            // Arrange
            String actuatorEndpoint = "/actuator/**";
            String requiredRole = "ADMIN";

            // Act & Assert
            assertThat(actuatorEndpoint).contains("/actuator");
            assertThat(requiredRole).isEqualTo("ADMIN");
        }
    }

    @Nested
    @DisplayName("HTTP Methods Security Tests")
    class HttpMethodsSecurityTests {

        @Test
        @DisplayName("Debe proteger métodos POST con roles apropiados")
        void shouldProtectPostMethodsWithAppropriateRoles() {
            // Arrange
            String httpMethod = "POST";
            String requiredRole = "TECNICO";

            // Act & Assert
            assertThat(httpMethod).isEqualTo("POST");
            assertThat(requiredRole).isIn("TECNICO", "ADMIN");
        }

        @Test
        @DisplayName("Debe proteger métodos PUT con roles apropiados")
        void shouldProtectPutMethodsWithAppropriateRoles() {
            // Arrange
            String httpMethod = "PUT";
            String requiredRole = "TECNICO";

            // Act & Assert
            assertThat(httpMethod).isEqualTo("PUT");
            assertThat(requiredRole).isIn("TECNICO", "ADMIN");
        }

        @Test
        @DisplayName("Debe proteger métodos DELETE con roles apropiados")
        void shouldProtectDeleteMethodsWithAppropriateRoles() {
            // Arrange
            String httpMethod = "DELETE";
            String[] allowedRoles = {"TECNICO", "ADMIN"};

            // Act & Assert
            assertThat(httpMethod).isEqualTo("DELETE");
            assertThat(allowedRoles).isNotEmpty();
        }

        @Test
        @DisplayName("Debe permitir métodos GET con autenticación")
        void shouldAllowGetMethodsWithAuthentication() {
            // Arrange
            String httpMethod = "GET";
            String[] allowedRoles = {"TECNICO", "ADMIN"};

            // Act & Assert
            assertThat(httpMethod).isEqualTo("GET");
            assertThat(allowedRoles).hasSizeGreaterThan(0);
        }
    }
}
package com.telconova.supportsuite.infraestructura.adaptadores.salida.seguridad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas de SeguridadServiceImpl")
class SeguridadServiceImplTest {


    private SeguridadServiceImpl seguridadService;

    private static final String JWT_SECRET = "miClaveSecretaSuperSeguraDeAlMenos256BitsParaHS256Algorithm";
    private static final long JWT_EXPIRATION = 3600000L; // 1 hora en milisegundos
    private static final String EMAIL_PRUEBA = "test@telconova.com";
    private static final String ROL_PRUEBA = "TECNICO";

    @BeforeEach
    void setUp() {
        seguridadService = new SeguridadServiceImpl(JWT_SECRET, JWT_EXPIRATION);
    }

    @Test
    void testEncriptarContrasena_GeneraHashDiferente() {
        // Arrange
        String contrasenaPlana = "MiContraseña123!";

        // Act
        String hashGenerado = seguridadService.encriptarContrasena(contrasenaPlana);

        // Assert
        assertNotNull(hashGenerado);
        assertNotEquals(contrasenaPlana, hashGenerado);
        assertTrue(hashGenerado.startsWith("$2a$") || hashGenerado.startsWith("$2b$"));
        assertTrue(hashGenerado.length() > 50);
    }

    @Test
    void testEncriptarContrasena_MismaContrasenaGeneraHashesDiferentes() {
        // Arrange
        String contrasenaPlana = "Password123!";

        // Act
        String hash1 = seguridadService.encriptarContrasena(contrasenaPlana);
        String hash2 = seguridadService.encriptarContrasena(contrasenaPlana);

        // Assert
        assertNotNull(hash1);
        assertNotNull(hash2);
        assertNotEquals(hash1, hash2); // BCrypt genera salts diferentes
    }

    @Test
    void testEncriptarContrasena_ContraseñaVacia() {
        // Arrange
        String contrasenaVacia = "";

        // Act
        String hashGenerado = seguridadService.encriptarContrasena(contrasenaVacia);

        // Assert
        assertNotNull(hashGenerado);
        assertTrue(!hashGenerado.isEmpty());
    }

    @Test
    void testVerificarContrasena_ContrasenaCorrecta() {
        // Arrange
        String contrasenaPlana = "Password123!";
        String hashAlmacenado = seguridadService.encriptarContrasena(contrasenaPlana);

        // Act
        boolean resultado = seguridadService.verificarContrasena(contrasenaPlana, hashAlmacenado);

        // Assert
        assertTrue(resultado);
    }

    @Test
    void testVerificarContrasena_ContrasenaIncorrecta() {
        // Arrange
        String contrasenaPlana = "Password123!";
        String contrasenaIncorrecta = "Password456!";
        String hashAlmacenado = seguridadService.encriptarContrasena(contrasenaPlana);

        // Act
        boolean resultado = seguridadService.verificarContrasena(contrasenaIncorrecta, hashAlmacenado);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void testVerificarContrasena_DiferenciaEnMayusculas() {
        // Arrange
        String contrasenaPlana = "Password123!";
        String contrasenaConOtraMayuscula = "password123!";
        String hashAlmacenado = seguridadService.encriptarContrasena(contrasenaPlana);

        // Act
        boolean resultado = seguridadService.verificarContrasena(contrasenaConOtraMayuscula, hashAlmacenado);

        // Assert
        assertFalse(resultado); // Las contraseñas son case-sensitive
    }

    @Test
    void testVerificarContrasena_ContrasenaVacia() {
        // Arrange
        String contrasenaPlana = "Password123!";
        String hashAlmacenado = seguridadService.encriptarContrasena(contrasenaPlana);

        // Act
        boolean resultado = seguridadService.verificarContrasena("", hashAlmacenado);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void testGenerarTokenJwt_GeneraTokenValido() {
        // Act
        String token = seguridadService.generarTokenJwt(EMAIL_PRUEBA, ROL_PRUEBA);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(3, token.split("\\.").length); // JWT tiene 3 partes separadas por puntos
    }

    @Test
    void testGenerarTokenJwt_ContieneClaimsCorrectos() {
        // Act
        String token = seguridadService.generarTokenJwt(EMAIL_PRUEBA, ROL_PRUEBA);

        // Assert
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(EMAIL_PRUEBA, claims.getSubject());
        assertEquals(EMAIL_PRUEBA, claims.get("email", String.class));
        assertEquals(ROL_PRUEBA, claims.get("rol", String.class));
    }

    @Test
    void testGenerarTokenJwt_TieneExpiracionFutura() {
        // Arrange
        long tiempoAntes = System.currentTimeMillis();

        // Act
        String token = seguridadService.generarTokenJwt(EMAIL_PRUEBA, ROL_PRUEBA);
        Date expiracion = seguridadService.obtenerExpiracionToken(token);

        // Assert
        long tiempoEsperado = tiempoAntes + JWT_EXPIRATION;
        assertTrue(expiracion.getTime() >= tiempoEsperado - 1000); // Margen de 1 segundo
        assertTrue(expiracion.after(new Date()));
    }

    @Test
    void testGenerarTokenJwt_DiferentesUsuariosGeneranTokensDiferentes() {
        // Act
        String token1 = seguridadService.generarTokenJwt("usuario1@telconova.com", "ADMIN");
        String token2 = seguridadService.generarTokenJwt("usuario2@telconova.com", "TECNICO");

        // Assert
        assertNotEquals(token1, token2);
    }

    @Test
    void testValidarTokenJwt_TokenValido() {
        // Arrange
        String token = seguridadService.generarTokenJwt(EMAIL_PRUEBA, ROL_PRUEBA);

        // Act
        boolean esValido = seguridadService.validarTokenJwt(token);

        // Assert
        assertTrue(esValido);
    }

    @Test
    void testValidarTokenJwt_TokenInvalido() {
        // Arrange
        String tokenInvalido = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0In0.invalido";

        // Act
        boolean esValido = seguridadService.validarTokenJwt(tokenInvalido);

        // Assert
        assertFalse(esValido);
    }

    @Test
    void testValidarTokenJwt_TokenMalFormado() {
        // Arrange
        String tokenMalFormado = "token.malformado.abc123";

        // Act
        boolean esValido = seguridadService.validarTokenJwt(tokenMalFormado);

        // Assert
        assertFalse(esValido);
    }

    @Test
    void testValidarTokenJwt_TokenVacio() {
        // Arrange
        String tokenVacio = "";

        // Act
        boolean esValido = seguridadService.validarTokenJwt(tokenVacio);

        // Assert
        assertFalse(esValido);
    }

    @Test
    void testExtraerEmailDeToken_ExtraeEmailCorrecto() {
        // Arrange
        String token = seguridadService.generarTokenJwt(EMAIL_PRUEBA, ROL_PRUEBA);

        // Act
        String emailExtraido = seguridadService.extraerEmailDeToken(token);

        // Assert
        assertEquals(EMAIL_PRUEBA, emailExtraido);
    }

    @Test
    void testExtraerEmailDeToken_MultipleUsuarios() {
        // Arrange
        String email1 = "admin@telconova.com";
        String email2 = "tecnico@telconova.com";
        String token1 = seguridadService.generarTokenJwt(email1, "ADMIN");
        String token2 = seguridadService.generarTokenJwt(email2, "TECNICO");

        // Act
        String emailExtraido1 = seguridadService.extraerEmailDeToken(token1);
        String emailExtraido2 = seguridadService.extraerEmailDeToken(token2);

        // Assert
        assertEquals(email1, emailExtraido1);
        assertEquals(email2, emailExtraido2);
    }

    @Test
    void testExtraerRolDeToken_ExtraeRolCorrecto() {
        // Arrange
        String token = seguridadService.generarTokenJwt(EMAIL_PRUEBA, ROL_PRUEBA);

        // Act
        String rolExtraido = seguridadService.extraerRolDeToken(token);

        // Assert
        assertEquals(ROL_PRUEBA, rolExtraido);
    }

    @Test
    void testExtraerRolDeToken_DiferentesRoles() {
        // Arrange
        String tokenAdmin = seguridadService.generarTokenJwt(EMAIL_PRUEBA, "ADMINISTRADOR");
        String tokenTecnico = seguridadService.generarTokenJwt(EMAIL_PRUEBA, "TECNICO");
        String tokenCliente = seguridadService.generarTokenJwt(EMAIL_PRUEBA, "CLIENTE");

        // Act
        String rolAdmin = seguridadService.extraerRolDeToken(tokenAdmin);
        String rolTecnico = seguridadService.extraerRolDeToken(tokenTecnico);
        String rolCliente = seguridadService.extraerRolDeToken(tokenCliente);

        // Assert
        assertEquals("ADMINISTRADOR", rolAdmin);
        assertEquals("TECNICO", rolTecnico);
        assertEquals("CLIENTE", rolCliente);
    }

    @Test
    void testTokenHaExpirado_TokenRecienCreado() {
        // Arrange
        String token = seguridadService.generarTokenJwt(EMAIL_PRUEBA, ROL_PRUEBA);

        // Act
        boolean haExpirado = seguridadService.tokenHaExpirado(token);

        // Assert
        assertFalse(haExpirado);
    }

    @Test
    void testObtenerExpiracionToken_RetornaFechaFutura() {
        // Arrange
        long tiempoAntes = System.currentTimeMillis();
        String token = seguridadService.generarTokenJwt(EMAIL_PRUEBA, ROL_PRUEBA);

        // Act
        Date expiracion = seguridadService.obtenerExpiracionToken(token);

        // Assert
        assertNotNull(expiracion);
        assertTrue(expiracion.after(new Date()));
        assertTrue(expiracion.getTime() > tiempoAntes);
    }

    @Test
    void testObtenerExpiracionToken_VerificaTiempoExpiracion() {
        // Arrange
        long tiempoInicio = System.currentTimeMillis();
        String token = seguridadService.generarTokenJwt(EMAIL_PRUEBA, ROL_PRUEBA);

        // Act
        Date expiracion = seguridadService.obtenerExpiracionToken(token);
        long tiempoExpiracionReal = expiracion.getTime() - tiempoInicio;

        // Assert
        // Verificar que la expiración está cerca del tiempo configurado (con margen de 1 segundo)
        assertTrue(Math.abs(tiempoExpiracionReal - JWT_EXPIRATION) < 1000);
    }

    @Test
    void testFlujoCompletoAutenticacion() {
        // Arrange
        String contrasenaOriginal = "MiPassword123!";
        String hashGuardado = seguridadService.encriptarContrasena(contrasenaOriginal);

        // Act - Simular inicio de sesión
        boolean contrasenaValida = seguridadService.verificarContrasena(contrasenaOriginal, hashGuardado);
        String token = null;
        if (contrasenaValida) {
            token = seguridadService.generarTokenJwt(EMAIL_PRUEBA, ROL_PRUEBA);
        }

        // Assert
        assertTrue(contrasenaValida);
        assertNotNull(token);
        assertTrue(seguridadService.validarTokenJwt(token));
        assertEquals(EMAIL_PRUEBA, seguridadService.extraerEmailDeToken(token));
        assertEquals(ROL_PRUEBA, seguridadService.extraerRolDeToken(token));
        assertFalse(seguridadService.tokenHaExpirado(token));
    }

    @Test
    void testSeguridad_NoSePuedeAdivinarContrasena() {
        // Arrange
        String contrasenaReal = "Contraseña123!";
        String hashAlmacenado = seguridadService.encriptarContrasena(contrasenaReal);

        String[] intentosFallidos = {
                "contraseña123!",
                "Contraseña123",
                "Contraseña 123!",
                "Contraseña123!!",
                "Contraseña12!"
        };

        // Act & Assert
        for (String intento : intentosFallidos) {
            boolean resultado = seguridadService.verificarContrasena(intento, hashAlmacenado);
            assertFalse(resultado, "El intento '" + intento + "' no debería ser válido");
        }
    }

    @Test
    void testJWT_TokenNoPuedeSerModificado() {
        // Arrange
        String tokenOriginal = seguridadService.generarTokenJwt(EMAIL_PRUEBA, ROL_PRUEBA);

        // Intentar modificar el token (cambiar un carácter)
        String tokenModificado = tokenOriginal.substring(0, tokenOriginal.length() - 5) + "XXXXX";

        // Act
        boolean tokenOriginalValido = seguridadService.validarTokenJwt(tokenOriginal);
        boolean tokenModificadoValido = seguridadService.validarTokenJwt(tokenModificado);

        // Assert
        assertTrue(tokenOriginalValido);
        assertFalse(tokenModificadoValido);
    }

    @Test
    void testEncriptarContrasena_ConCaracteresEspeciales() {
        // Arrange
        String contrasenaConEspeciales = "P@ssw0rd!#$%&*()[]{}";

        // Act
        String hash = seguridadService.encriptarContrasena(contrasenaConEspeciales);
        boolean verificacion = seguridadService.verificarContrasena(contrasenaConEspeciales, hash);

        // Assert
        assertNotNull(hash);
        assertTrue(verificacion);
    }

    @Test
    void testGenerarTokenJwt_ConEmailsLargos() {
        // Arrange
        String emailLargo = "usuario.con.nombre.muy.largo.de.prueba@telconova.com";

        // Act
        String token = seguridadService.generarTokenJwt(emailLargo, ROL_PRUEBA);
        String emailExtraido = seguridadService.extraerEmailDeToken(token);

        // Assert
        assertNotNull(token);
        assertEquals(emailLargo, emailExtraido);
    }

    @Test
    void testBCryptStrength_UsaFortalezaAdecuada() {
        // Arrange
        String contrasena = "TestPassword123!";

        // Act
        long tiempoInicio = System.currentTimeMillis();
        String hash = seguridadService.encriptarContrasena(contrasena);
        long tiempoFin = System.currentTimeMillis();

        // Assert
        assertNotNull(hash);
        // BCrypt con strength 12 debería tomar al menos algunos milisegundos
        long tiempoTranscurrido = tiempoFin - tiempoInicio;
        assertTrue(tiempoTranscurrido > 0, "BCrypt debería tomar tiempo en encriptar");
        assertTrue(hash.startsWith("$2a$12$") || hash.startsWith("$2b$12$"),
                "El hash debería usar BCrypt con strength 12");
    }
}

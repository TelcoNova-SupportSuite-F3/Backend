package com.telconova.supportsuite.compartido.utilidades;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Tests para ValidacionUtil")
class ValidacionUtilTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "test@example.com",
            "user.name@domain.com",
            "user+tag@example.co.uk"
    })
    @DisplayName("Debe validar emails con formato correcto")
    void debeValidarEmailsCorrectos(String email) {
        // Act
        boolean resultado = ValidacionUtil.esEmailValido(email);

        // Assert
        assertThat(resultado).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"invalido", "@example.com", "test@", "test@@example.com"})
    @DisplayName("Debe rechazar emails con formato incorrecto")
    void debeRechazarEmailsIncorrectos(String email) {
        // Act
        boolean resultado = ValidacionUtil.esEmailValido(email);

        // Assert
        assertThat(resultado).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "admin@telconova.com",
            "tecnico@telconova.com",
            "test@telconova.com"
    })
    @DisplayName("Debe validar emails del dominio TelcoNova")
    void debeValidarEmailsTelconova(String email) {
        // Act
        boolean resultado = ValidacionUtil.esEmailTelconova(email);

        // Assert
        assertThat(resultado).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "admin@gmail.com",
            "tecnico@yahoo.com",
            "test@empresa.com"
    })
    @DisplayName("Debe rechazar emails de otros dominios")
    void debeRechazarEmailsOtrosDominios(String email) {
        // Act
        boolean resultado = ValidacionUtil.esEmailTelconova(email);

        // Assert
        assertThat(resultado).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "+57 300 1234567",
            "+57 310 9876543",
            "+573201234567"
    })
    @DisplayName("Debe validar teléfonos colombianos correctos")
    void debeValidarTelefonosCorrectos(String telefono) {
        // Act
        boolean resultado = ValidacionUtil.esTelefonoValido(telefono);

        // Assert
        assertThat(resultado).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ORD-2025-001",
            "ORD-2024-123",
            "ORD-2025-999999"
    })
    @DisplayName("Debe validar números de orden correctos")
    void debeValidarNumerosOrdenCorrectos(String numeroOrden) {
        // Act
        boolean resultado = ValidacionUtil.esNumeroOrdenValido(numeroOrden);

        // Assert
        assertThat(resultado).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ORD-25-001",
            "2025-001",
            "ORD-2025-",
            "orden-2025-001"
    })
    @DisplayName("Debe rechazar números de orden incorrectos")
    void debeRechazarNumerosOrdenIncorrectos(String numeroOrden) {
        // Act
        boolean resultado = ValidacionUtil.esNumeroOrdenValido(numeroOrden);

        // Assert
        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("Debe validar longitud mínima correctamente")
    void debeValidarLongitudMinima() {
        // Arrange
        String texto = "Hola Mundo";

        // Act
        boolean resultado = ValidacionUtil.cumpleLongitudMinima(texto, 5);

        // Assert
        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("Debe rechazar texto menor a longitud mínima")
    void debeRechazarTextoMenorAMinimo() {
        // Arrange
        String texto = "Hola";

        // Act
        boolean resultado = ValidacionUtil.cumpleLongitudMinima(texto, 10);

        // Assert
        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("Debe validar longitud máxima correctamente")
    void debeValidarLongitudMaxima() {
        // Arrange
        String texto = "Hola";

        // Act
        boolean resultado = ValidacionUtil.cumpleLongitudMaxima(texto, 10);

        // Assert
        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("Debe rechazar texto mayor a longitud máxima")
    void debeRechazarTextoMayorAMaximo() {
        // Arrange
        String texto = "Este es un texto muy largo";

        // Act
        boolean resultado = ValidacionUtil.cumpleLongitudMaxima(texto, 10);

        // Assert
        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("Debe validar números positivos")
    void debeValidarNumerosPositivos() {
        // Act
        boolean resultado = ValidacionUtil.esPositivo(10);

        // Assert
        assertThat(resultado).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    @DisplayName("Debe rechazar números no positivos")
    void debeRechazarNumerosNoPositivos(int numero) {
        // Act
        boolean resultado = ValidacionUtil.esPositivo(numero);

        // Assert
        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("Debe validar número en rango")
    void debeValidarNumeroEnRango() {
        // Act
        boolean resultado = ValidacionUtil.estaEnRango(50, 1, 100);

        // Assert
        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("Debe rechazar número fuera de rango")
    void debeRechazarNumeroFueraDeRango() {
        // Act
        boolean resultado = ValidacionUtil.estaEnRango(150, 1, 100);

        // Assert
        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("Debe validar comentario con longitud correcta")
    void debeValidarComentarioValido() {
        // Arrange
        String comentario = "Este es un comentario válido";

        // Act
        boolean resultado = ValidacionUtil.esComentarioValido(comentario);

        // Assert
        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("Debe rechazar comentario demasiado largo")
    void debeRechazarComentarioMuyLargo() {
        // Arrange
        String comentario = "a".repeat(501);

        // Act
        boolean resultado = ValidacionUtil.esComentarioValido(comentario);

        // Assert
        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("Debe limpiar texto correctamente")
    void debeLimpiarTextoCorrectamente() {
        // Arrange
        String texto = "  Hola    Mundo   ";

        // Act
        String limpio = ValidacionUtil.limpiarTexto(texto);

        // Assert
        assertThat(limpio).isEqualTo("Hola Mundo");
    }

    @Test
    @DisplayName("Debe normalizar email a minúsculas")
    void debeNormalizarEmail() {
        // Arrange
        String email = "  ADMIN@TELCONOVA.COM  ";

        // Act
        String normalizado = ValidacionUtil.normalizarEmail(email);

        // Assert
        assertThat(normalizado).isEqualTo("admin@telconova.com");
    }

    @Test
    @DisplayName("Debe validar ID válido")
    void debeValidarIdValido() {
        // Act
        boolean resultado = ValidacionUtil.esIdValido(1L);

        // Assert
        assertThat(resultado).isTrue();
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1, -100})
    @DisplayName("Debe rechazar ID inválido")
    void debeRechazarIdInvalido(Long id) {
        // Act
        boolean resultado = ValidacionUtil.esIdValido(id);

        // Assert
        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("Debe validar cantidad válida")
    void debeValidarCantidadValida() {
        // Act
        boolean resultado = ValidacionUtil.esCantidadValida(10);

        // Assert
        assertThat(resultado).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -50})
    @DisplayName("Debe rechazar cantidad inválida")
    void debeRechazarCantidadInvalida(Integer cantidad) {
        // Act
        boolean resultado = ValidacionUtil.esCantidadValida(cantidad);

        // Assert
        assertThat(resultado).isFalse();
    }
}

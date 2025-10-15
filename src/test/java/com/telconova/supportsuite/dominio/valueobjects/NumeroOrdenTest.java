package com.telconova.supportsuite.dominio.valueobjects;

import com.telconova.supportsuite.dominio.excepciones.DominioExcepcion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Tests para NumeroOrden Value Object")
class NumeroOrdenTest {

    @Test
    @DisplayName("Debe crear número de orden válido")
    void debeCrearNumeroOrdenValido() {
        // Arrange
        String numeroValido = "ORD-2025-001";

        // Act
        NumeroOrden numeroOrden = NumeroOrden.de(numeroValido);

        // Assert
        assertThat(numeroOrden).isNotNull();
        assertThat(numeroOrden.getValor()).isEqualTo("ORD-2025-001");
        assertThat(numeroOrden).hasToString("ORD-2025-001");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    @DisplayName("Debe lanzar excepción con número vacío o nulo")
    void debeLanzarExcepcionConNumeroVacio(String numeroInvalido) {
        // Act & Assert
        assertThatThrownBy(() -> NumeroOrden.de(numeroInvalido))
                .isInstanceOf(DominioExcepcion.class)
                .hasMessageContaining("no puede estar vacío");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ORD-2025",
            "2025-001",
            "ORD-25-001",
            "ORD-2025-",
            "orden-2025-001",
            "ORD-202A-001",
            "ORD-2025-ABC"
    })
    @DisplayName("Debe lanzar excepción con formato inválido")
    void debeLanzarExcepcionConFormatoInvalido(String numeroInvalido) {
        // Act & Assert
        assertThatThrownBy(() -> NumeroOrden.de(numeroInvalido))
                .isInstanceOf(DominioExcepcion.class)
                .hasMessageContaining("formato");
    }

    @Test
    @DisplayName("Debe normalizar a mayúsculas")
    void debeNormalizarAMayusculas() {
        // Arrange
        String numeroMinusculas = "ord-2025-001";

        // Act
        NumeroOrden numeroOrden = NumeroOrden.de(numeroMinusculas);

        // Assert
        assertThat(numeroOrden.getValor()).isEqualTo("ORD-2025-001");
    }

    @Test
    @DisplayName("Debe generar número de orden automáticamente")
    void debeGenerarNumeroOrdenAutomaticamente() {
        // Arrange
        int secuencial = 123;

        // Act
        NumeroOrden numeroOrden = NumeroOrden.generar(secuencial);

        // Assert
        assertThat(numeroOrden.getValor()).matches("ORD-\\d{4}-123");
        assertThat(numeroOrden.getSecuencial()).isEqualTo(123);
    }

    @Test
    @DisplayName("Debe lanzar excepción al generar con secuencial inválido")
    void debeLanzarExcepcionConSecuencialInvalido() {
        // Arrange
        int secuencialInvalido = 0;

        // Act & Assert
        assertThatThrownBy(() -> NumeroOrden.generar(secuencialInvalido))
                .isInstanceOf(DominioExcepcion.class)
                .hasMessageContaining("mayor a cero");
    }

    @Test
    @DisplayName("Debe obtener año correctamente")
    void debeObtenerAnio() {
        // Arrange
        NumeroOrden numeroOrden = NumeroOrden.de("ORD-2025-001");

        // Act
        String anio = numeroOrden.getAnio();

        // Assert
        assertThat(anio).isEqualTo("2025");
    }

    @Test
    @DisplayName("Debe obtener secuencial correctamente")
    void debeObtenerSecuencial() {
        // Arrange
        NumeroOrden numeroOrden = NumeroOrden.de("ORD-2025-123");

        // Act
        int secuencial = numeroOrden.getSecuencial();

        // Assert
        assertThat(secuencial).isEqualTo(123);
    }

    @Test
    @DisplayName("Debe verificar si es del año actual")
    void debeVerificarSiEsDelAnioActual() {
        // Arrange
        int anioActual = LocalDateTime.now().getYear();
        NumeroOrden numeroOrden = NumeroOrden.de("ORD-" + anioActual + "-001");

        // Act
        boolean esDelAnioActual = numeroOrden.esDelAnioActual();

        // Assert
        assertThat(esDelAnioActual).isTrue();
    }

    @Test
    @DisplayName("Debe verificar que no es del año actual")
    void debeVerificarQueNoEsDelAnioActual() {
        // Arrange
        NumeroOrden numeroOrden = NumeroOrden.de("ORD-2020-001");

        // Act
        boolean esDelAnioActual = numeroOrden.esDelAnioActual();

        // Assert
        assertThat(esDelAnioActual).isFalse();
    }

    @Test
    @DisplayName("Debe aceptar secuencial con hasta 6 dígitos")
    void debeAceptarSecuencialLargo() {
        // Arrange
        String numeroLargo = "ORD-2025-123456";

        // Act
        NumeroOrden numeroOrden = NumeroOrden.de(numeroLargo);

        // Assert
        assertThat(numeroOrden.getSecuencial()).isEqualTo(123456);
    }

    @Test
    @DisplayName("Dos números de orden iguales deben ser iguales")
    void dosNumerosOrdenIgualesDebenSerIguales() {
        // Arrange
        NumeroOrden numero1 = NumeroOrden.de("ORD-2025-001");
        NumeroOrden numero2 = NumeroOrden.de("ord-2025-001");

        // Act & Assert
        assertThat(numero1).isEqualTo(numero2)
                .hasSameHashCodeAs(numero2);
    }
}

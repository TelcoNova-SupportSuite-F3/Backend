package com.telconova.supportsuite.dominio.valueobjects;

import com.telconova.supportsuite.dominio.excepciones.DominioExcepcion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Tests para Telefono Value Object")
class TelefonoTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "+57 300 1234567",
            "+57 310 9876543",
            "+57 320 5555555"
    })
    @DisplayName("Debe crear teléfono móvil válido")
    void debeCrearTelefonoMovilValido(String telefonoValido) {
        // Act
        Telefono telefono = Telefono.de(telefonoValido);

        // Assert
        assertThat(telefono).isNotNull();
        assertThat(telefono.esMovil()).isTrue();
        assertThat(telefono.esFijo()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "+57 4 1234567",
            "+57 1 7654321",
            "+57 2 9999999"
    })
    @DisplayName("Debe crear teléfono fijo válido")
    void debeCrearTelefonoFijoValido(String telefonoValido) {
        // Act
        Telefono telefono = Telefono.de(telefonoValido);

        // Assert
        assertThat(telefono).isNotNull();
        assertThat(telefono.esFijo()).isTrue();
        assertThat(telefono.esMovil()).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    @DisplayName("Debe lanzar excepción con teléfono vacío o nulo")
    void debeLanzarExcepcionConTelefonoVacio(String telefonoInvalido) {
        // Act & Assert
        assertThatThrownBy(() -> Telefono.de(telefonoInvalido))
                .isInstanceOf(DominioExcepcion.class)
                .hasMessageContaining("no puede estar vacío");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "300 1234567",
            "57 300 1234567",
            "+57 100 1234567",
            "+57 400 123456",
            "+57 300 12345678",
            "+1 300 1234567",
            "123456789"
    })
    @DisplayName("Debe lanzar excepción con formato inválido")
    void debeLanzarExcepcionConFormatoInvalido(String telefonoInvalido) {
        // Act & Assert
        assertThatThrownBy(() -> Telefono.de(telefonoInvalido))
                .isInstanceOf(DominioExcepcion.class)
                .hasMessageContaining("formato");
    }

    @Test
    @DisplayName("Debe crear teléfono sin validación para datos legacy")
    void debeCrearTelefonoSinValidacion() {
        // Arrange
        String telefonoLegacy = "123-456-7890";

        // Act
        Telefono telefono = Telefono.deSinValidacion(telefonoLegacy);

        // Assert
        assertThat(telefono).isNotNull();
        assertThat(telefono.getValor()).isEqualTo("123-456-7890");
    }

    @Test
    @DisplayName("Debe retornar null al crear sin validación con valor nulo")
    void debeRetornarNullAlCrearSinValidacionConNull() {
        // Act
        Telefono telefono = Telefono.deSinValidacion(null);

        // Assert
        assertThat(telefono).isNull();
    }

    @Test
    @DisplayName("Debe obtener número sin formato")
    void debeObtenerNumeroSinFormato() {
        // Arrange
        Telefono telefono = Telefono.de("+57 300 1234567");

        // Act
        String sinFormato = telefono.getSinFormato();

        // Assert
        assertThat(sinFormato).isEqualTo("+573001234567");
    }

    @Test
    @DisplayName("Debe formatear teléfono móvil correctamente")
    void debeFormatearTelefonoMovil() {
        // Arrange
        Telefono telefono = Telefono.de("+573001234567");

        // Act
        String formateado = telefono.getFormateado();

        // Assert
        assertThat(formateado).matches("\\+57 3\\d{2} \\d{3} \\d{4}");
    }

    @Test
    @DisplayName("Debe formatear teléfono fijo correctamente")
    void debeFormatearTelefonoFijo() {
        // Arrange
        Telefono telefono = Telefono.de("+5741234567");

        // Act
        String formateado = telefono.getFormateado();

        // Assert
        assertThat(formateado).matches("\\+57 \\d \\d{3} \\d{4}");
    }

    @Test
    @DisplayName("Dos teléfonos iguales deben ser iguales")
    void dosTelefonosIgualesDebenSerIguales() {
        // Arrange
        Telefono telefono1 = Telefono.de("+57 300 1234567");
        Telefono telefono2 = Telefono.de("+57 300 1234567");

        // Act & Assert
        assertThat(telefono1).isEqualTo(telefono2)
                .hasSameHashCodeAs(telefono2);
    }

    @Test
    @DisplayName("toString debe retornar el valor del teléfono")
    void toStringDebeRetornarValor() {
        // Arrange
        Telefono telefono = Telefono.de("+57 300 1234567");

        // Act
        String resultado = telefono.toString();

        // Assert
        assertThat(resultado).isEqualTo("+57 300 1234567");
    }
}

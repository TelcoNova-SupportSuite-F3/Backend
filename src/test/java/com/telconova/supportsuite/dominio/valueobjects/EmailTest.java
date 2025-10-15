package com.telconova.supportsuite.dominio.valueobjects;

import com.telconova.supportsuite.dominio.excepciones.DominioExcepcion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Tests para Email Value Object")
class EmailTest {

    @Test
    @DisplayName("Debe crear email válido del dominio TelcoNova")
    void debeCrearEmailValido() {
        // Arrange
        String emailValido = "tecnico@telconova.com";

        // Act
        Email email = Email.de(emailValido);

        // Assert
        assertThat(email).isNotNull();
        assertThat(email.getValor()).isEqualTo("tecnico@telconova.com");
        assertThat(email).hasToString("tecnico@telconova.com");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Debe lanzar excepción con email vacío o nulo")
    void debeLanzarExcepcionConEmailVacio(String emailInvalido) {
        // Act & Assert
        assertThatThrownBy(() -> Email.de(emailInvalido))
                .isInstanceOf(DominioExcepcion.class)
                .hasMessageContaining("no puede estar vacío");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "invalido",
            "invalido@",
            "@telconova.com",
            "invalido@telconova",
            "invalido@@telconova.com",
            "invalido telconova.com"
    })
    @DisplayName("Debe lanzar excepción con formato inválido")
    void debeLanzarExcepcionConFormatoInvalido(String emailInvalido) {
        // Act & Assert
        assertThatThrownBy(() -> Email.de(emailInvalido))
                .isInstanceOf(DominioExcepcion.class)
                .hasMessageContaining("formato del email no es válido");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "tecnico@gmail.com",
            "admin@yahoo.com",
            "usuario@hotmail.com",
            "test@empresa.com"
    })
    @DisplayName("Debe lanzar excepción con dominio diferente a TelcoNova")
    void debeLanzarExcepcionConDominioInvalido(String emailInvalido) {
        // Act & Assert
        assertThatThrownBy(() -> Email.de(emailInvalido))
                .isInstanceOf(DominioExcepcion.class)
                .hasMessageContaining("@telconova.com");
    }

    @Test
    @DisplayName("Debe normalizar email a minúsculas")
    void debeNormalizarEmailAMinusculas() {
        // Arrange
        String emailMayusculas = "TECNICO@TELCONOVA.COM";

        // Act
        Email email = Email.de(emailMayusculas);

        // Assert
        assertThat(email.getValor()).isEqualTo("tecnico@telconova.com");
    }

    @Test
    @DisplayName("Debe eliminar espacios en blanco")
    void debeEliminarEspacios() {
        // Arrange
        String emailConEspacios = "  tecnico@telconova.com  ";

        // Act
        Email email = Email.de(emailConEspacios);

        // Assert
        assertThat(email.getValor()).isEqualTo("tecnico@telconova.com");
    }

    @Test
    @DisplayName("Debe verificar que es dominio TelcoNova")
    void debeVerificarDominioTelconova() {
        // Arrange
        Email email = Email.de("tecnico@telconova.com");

        // Act
        boolean esDominioTelconova = email.esDominioTelconova();

        // Assert
        assertThat(esDominioTelconova).isTrue();
    }

    @Test
    @DisplayName("Debe obtener nombre de usuario correctamente")
    void debeObtenerNombreUsuario() {
        // Arrange
        Email email = Email.de("juan.perez@telconova.com");

        // Act
        String nombreUsuario = email.getNombreUsuario();

        // Assert
        assertThat(nombreUsuario).isEqualTo("juan.perez");
    }

    @Test
    @DisplayName("Debe obtener dominio correctamente")
    void debeObtenerDominio() {
        // Arrange
        Email email = Email.de("tecnico@telconova.com");

        // Act
        String dominio = email.getDominio();

        // Assert
        assertThat(dominio).isEqualTo("telconova.com");
    }

    @Test
    @DisplayName("Dos emails con el mismo valor deben ser iguales")
    void dosEmailsIgualesDebenSerIguales() {
        // Arrange
        Email email1 = Email.de("tecnico@telconova.com");
        Email email2 = Email.de("TECNICO@telconova.com");

        // Act & Assert
        assertThat(email1).isEqualTo(email2)
                .hasSameHashCodeAs(email2);
    }

    @Test
    @DisplayName("Dos emails diferentes no deben ser iguales")
    void dosEmailsDiferentesNoDebenSerIguales() {
        // Arrange
        Email email1 = Email.de("tecnico1@telconova.com");
        Email email2 = Email.de("tecnico2@telconova.com");

        // Act & Assert
        assertThat(email1).isNotEqualTo(email2);
    }
}

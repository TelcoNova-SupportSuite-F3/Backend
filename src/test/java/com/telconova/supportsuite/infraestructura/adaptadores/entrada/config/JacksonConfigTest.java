package com.telconova.supportsuite.infraestructura.adaptadores.entrada.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para clase de configuración Jackson")
class JacksonConfigTest {

    @Test
    @DisplayName("Debe configurar ObjectMapper correctamente")
    void debeConfigurarObjectMapperCorrectamente() {
        // Arrange
        JacksonConfig jacksonConfig = new JacksonConfig();

        // Act
        ObjectMapper objectMapper = jacksonConfig.objectMapper();

        // Assert
        assertThat(objectMapper).isNotNull();
        assertThat(objectMapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)).isFalse();
    }

    @Test
    @DisplayName("Debe serializar LocalDateTime con formato correcto")
    void debeSerializarLocalDateTimeConFormatoCorrecto() throws Exception {
        // Arrange
        JacksonConfig jacksonConfig = new JacksonConfig();
        ObjectMapper objectMapper = jacksonConfig.objectMapper();
        LocalDateTime fecha = LocalDateTime.of(2025, 1, 15, 10, 30, 45);

        // Act
        String json = objectMapper.writeValueAsString(fecha);

        // Assert
        assertThat(json).contains("2025-01-15 10:30:45");
    }

    @Test
    @DisplayName("Debe deserializar LocalDateTime correctamente")
    void debeDeserializarLocalDateTimeCorrectamente() throws Exception {
        // Arrange
        JacksonConfig jacksonConfig = new JacksonConfig();
        ObjectMapper objectMapper = jacksonConfig.objectMapper();
        String json = "\"2025-01-15T10:30:45\"";

        // Act
        LocalDateTime fecha = objectMapper.readValue(json, LocalDateTime.class);

        // Assert
        assertThat(fecha).isNotNull();
        assertThat(fecha.getYear()).isEqualTo(2025);
        assertThat(fecha.getMonthValue()).isEqualTo(1);
        assertThat(fecha.getDayOfMonth()).isEqualTo(15);
        assertThat(fecha.getHour()).isEqualTo(10);
        assertThat(fecha.getMinute()).isEqualTo(30);
        assertThat(fecha.getSecond()).isEqualTo(45);
    }
}

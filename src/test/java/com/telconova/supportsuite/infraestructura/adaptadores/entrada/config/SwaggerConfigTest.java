package com.telconova.supportsuite.infraestructura.adaptadores.entrada.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para clase de configuración Swagger")
class SwaggerConfigTest {

    @Test
    @DisplayName("Debe configurar OpenAPI para perfil desarrollo")
    void debeConfigurarOpenAPIParaDesarrollo() {
        // Arrange
        SwaggerConfig swaggerConfig = new SwaggerConfig();
        ReflectionTestUtils.setField(swaggerConfig, "activeProfile", "dev");
        ReflectionTestUtils.setField(swaggerConfig, "developmentUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(swaggerConfig, "testingUrl", "https://api-test.telconova.com");
        ReflectionTestUtils.setField(swaggerConfig, "productionUrl", "https://backendtelconova-production.up.railway.app");

        // Act
        var openAPI = swaggerConfig.customOpenAPI();

        // Assert
        assertThat(openAPI).isNotNull();
        assertThat(openAPI.getInfo()).isNotNull();
        assertThat(openAPI.getInfo().getTitle()).isEqualTo("TelcoNova SupportSuite API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.0.0");
        assertThat(openAPI.getInfo().getContact()).isNotNull();
        assertThat(openAPI.getInfo().getContact().getName()).isEqualTo("Equipo de Desarrollo TelcoNova");
        assertThat(openAPI.getInfo().getContact().getEmail()).isEqualTo("desarrollo@telconova.com");
        assertThat(openAPI.getInfo().getLicense()).isNotNull();
        assertThat(openAPI.getInfo().getLicense().getName()).isEqualTo("Propietario");
        assertThat(openAPI.getServers()).isNotEmpty();
        assertThat(openAPI.getServers().get(0).getUrl()).isEqualTo("http://localhost:8080");
        assertThat(openAPI.getServers().get(0).getDescription()).contains("Desarrollo");
    }

    @Test
    @DisplayName("Debe configurar OpenAPI para perfil producción")
    void debeConfigurarOpenAPIParaProduccion() {
        // Arrange
        SwaggerConfig swaggerConfig = new SwaggerConfig();
        ReflectionTestUtils.setField(swaggerConfig, "activeProfile", "prod");
        ReflectionTestUtils.setField(swaggerConfig, "developmentUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(swaggerConfig, "testingUrl", "https://api-test.telconova.com");
        ReflectionTestUtils.setField(swaggerConfig, "productionUrl", "https://backendtelconova-production.up.railway.app");

        // Act
        var openAPI = swaggerConfig.customOpenAPI();

        // Assert
        assertThat(openAPI).isNotNull();
        assertThat(openAPI.getServers()).isNotEmpty();
        assertThat(openAPI.getServers().get(0).getUrl()).isEqualTo("https://backendtelconova-production.up.railway.app");
        assertThat(openAPI.getServers().get(0).getDescription()).contains("Producción");
    }

    @Test
    @DisplayName("Debe configurar OpenAPI para perfil testing")
    void debeConfigurarOpenAPIParaTesting() {
        // Arrange
        SwaggerConfig swaggerConfig = new SwaggerConfig();
        ReflectionTestUtils.setField(swaggerConfig, "activeProfile", "test");
        ReflectionTestUtils.setField(swaggerConfig, "developmentUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(swaggerConfig, "testingUrl", "https://api-test.telconova.com");
        ReflectionTestUtils.setField(swaggerConfig, "productionUrl", "https://backendtelconova-production.up.railway.app");

        // Act
        var openAPI = swaggerConfig.customOpenAPI();

        // Assert
        assertThat(openAPI).isNotNull();
        assertThat(openAPI.getServers()).isNotEmpty();
        assertThat(openAPI.getServers().get(0).getUrl()).isEqualTo("https://api-test.telconova.com");
        assertThat(openAPI.getServers().get(0).getDescription()).contains("Pruebas");
    }

    @Test
    @DisplayName("Debe configurar esquemas de seguridad correctamente")
    void debeConfigurarEsquemasSeguridadCorrectamente() {
        // Arrange
        SwaggerConfig swaggerConfig = new SwaggerConfig();
        ReflectionTestUtils.setField(swaggerConfig, "activeProfile", "dev");
        ReflectionTestUtils.setField(swaggerConfig, "developmentUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(swaggerConfig, "testingUrl", "https://api-test.telconova.com");
        ReflectionTestUtils.setField(swaggerConfig, "productionUrl", "https://api.telconova.com");

        // Act
        var openAPI = swaggerConfig.customOpenAPI();

        // Assert
        assertThat(openAPI.getComponents()).isNotNull();
        assertThat(openAPI.getComponents().getSecuritySchemes()).isNotEmpty();
        assertThat(openAPI.getComponents().getSecuritySchemes()).containsKey("Bearer Authentication");

        var securityScheme = openAPI.getComponents().getSecuritySchemes().get("Bearer Authentication");
        assertThat(securityScheme.getType()).hasToString("http");
        assertThat(securityScheme.getScheme()).isEqualTo("bearer");
        assertThat(securityScheme.getBearerFormat()).isEqualTo("JWT");
    }

    @Test
    @DisplayName("Debe incluir esquema MultipartFile")
    void debeIncluirEsquemaMultipartFile() {
        // Arrange
        SwaggerConfig swaggerConfig = new SwaggerConfig();
        ReflectionTestUtils.setField(swaggerConfig, "activeProfile", "dev");
        ReflectionTestUtils.setField(swaggerConfig, "developmentUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(swaggerConfig, "testingUrl", "https://api-test.telconova.com");
        ReflectionTestUtils.setField(swaggerConfig, "productionUrl", "https://api.telconova.com");

        // Act
        var openAPI = swaggerConfig.customOpenAPI();

        // Assert
        assertThat(openAPI.getComponents().getSchemas()).containsKey("MultipartFile");
        var multipartSchema = openAPI.getComponents().getSchemas().get("MultipartFile");
        assertThat(multipartSchema.getType()).isEqualTo("string");
        assertThat(multipartSchema.getFormat()).isEqualTo("binary");
    }
}

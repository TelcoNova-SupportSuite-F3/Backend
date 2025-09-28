package com.telconova.supportsuite.infraestructura.adaptadores.entrada.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuración de Swagger/OpenAPI
 */
@Slf4j
@Configuration
public class SwaggerConfig {

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Value("${api.base-url.development:http://localhost:8080}")
    private String developmentUrl;

    @Value("${api.base-url.testing:https://api-test.telconova.com}")
    private String testingUrl;

    @Value("${api.base-url.production:https://backendtelconova-production.up.railway.app}")
    private String productionUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title("TelcoNova SupportSuite API")
                        .version("1.0.0")
                        .description("""
                                ## API para el Sistema de Gestión de Soporte Técnico de TelcoNova Colombia
                                
                                **Feature 3: Seguimiento de Órdenes en Proceso**
                                
                                ### Funcionalidades principales:
                                - 🔐 **Autenticación**: Login con JWT para técnicos y administradores
                                - 📋 **Gestión de Órdenes**: Visualización y actualización de órdenes de trabajo
                                - 📸 **Evidencias**: Registro de comentarios y fotos
                                - 🔧 **Materiales**: Control de materiales utilizados
                                - 📊 **Dashboard**: Vista administrativa completa
                                
                                ### Roles disponibles:
                                - **TECNICO**: Acceso a sus órdenes asignadas
                                - **ADMIN**: Vista de todas las órdenes del sistema
                                
                                ### Autenticación:
                                1. Obtén tu token haciendo POST a `/auth/login`
                                2. Incluye el token en el header: `Authorization: Bearer {tu-token}`
                                3. Solo se permiten emails del dominio @telconova.com
                                
                                ### Nota importante:
                                - Asegúrate de seleccionar el servidor correcto en el dropdown de arriba
                                - Para producción usa: Railway (primera opción)
                                - Los endpoints de la API NO usan prefijo /api/v1 para auth
                                """)
                        .contact(new Contact()
                                .name("Equipo de Desarrollo TelcoNova")
                                .email("desarrollo@telconova.com")
                                .url("https://telconova.com"))
                        .license(new License()
                                .name("Propietario")
                                .url("https://telconova.com/licencia")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Ingresa tu token JWT obtenido del endpoint /auth/login"))
                        .addSchemas("MultipartFile", new Schema<>()
                                .type("string")
                                .format("binary")
                                .description("Archivo para subir")));

        openAPI.servers(buildServerList());

        return openAPI;
    }
    private List<Server> buildServerList() {
        List<Server> servers = new ArrayList<>();

        // Servidor principal según el perfil activo
        if ("prod".equals(activeProfile)) {
            servers.add(new Server()
                    .url(productionUrl)
                    .description("🚀 Servidor de Producción (Actual)"));
        } else if ("test".equals(activeProfile)) {
            servers.add(new Server()
                    .url(testingUrl)
                    .description("🧪 Servidor de Pruebas (Actual)"));
        } else {
            servers.add(new Server()
                    .url(developmentUrl)
                    .description("💻 Servidor de Desarrollo (Actual)"));
        }

        // Agregar otros servidores disponibles
        if (!"prod".equals(activeProfile)) {
            servers.add(new Server()
                    .url(productionUrl)
                    .description("🚀 Servidor de Producción"));
        }

        if (!"test".equals(activeProfile)) {
            servers.add(new Server()
                    .url(testingUrl)
                    .description("🧪 Servidor de Pruebas"));
        }

        if (!"dev".equals(activeProfile)) {
            servers.add(new Server()
                    .url(developmentUrl)
                    .description("💻 Servidor de Desarrollo"));
        }

        // Log de servidores configurados
        servers.forEach(server ->
                log.info("Servidor configurado: {} - {}", server.getDescription(), server.getUrl())
        );

        return servers;
    }
}

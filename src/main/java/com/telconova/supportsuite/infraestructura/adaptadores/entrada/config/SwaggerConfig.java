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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de Swagger/OpenAPI
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
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
                            """)
                        .contact(new Contact()
                                .name("Equipo de Desarrollo TelcoNova")
                                .email("desarrollo@telconova.com")
                                .url("https://telconova.com"))
                        .license(new License()
                                .name("Propietario")
                                .url("https://telconova.com/licencia")))
                .servers(List.of(
                        new Server().url("http://localhost:8080/api/v1").description("Servidor de Desarrollo"),
                        new Server().url("https://api-test.telconova.com/api/v1").description("Servidor de Pruebas"),
                        new Server().url("https://api.telconova.com/api/v1").description("Servidor de Producción")
                ))
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
    }
}

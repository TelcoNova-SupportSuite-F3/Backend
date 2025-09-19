package com.telconova.supportsuite;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@Slf4j
@EnableScheduling
@EnableTransactionManagement
@OpenAPIDefinition(
        info = @Info(
                title = "TelcoNova SupportSuite API",
                version = "1.0.0",
                description = """
            API para el Sistema de Gestión de Soporte Técnico de TelcoNova Colombia.
            
            **Feature 3: Seguimiento de Órdenes en Proceso**
            
            Esta API permite a los técnicos:
            - Autenticarse en el sistema
            - Ver sus órdenes de trabajo asignadas
            - Registrar avances y evidencias
            - Agregar materiales utilizados
            - Actualizar el estado de las órdenes
            
            **Roles disponibles:**
            - TECNICO: Acceso a sus órdenes asignadas
            - ADMIN: Vista de todas las órdenes del sistema
            """,
                contact = @Contact(
                        name = "Equipo de Desarrollo Universidad de Antioquia - Fabrica Escuela 2025-2",
                        email = "darwin.tangarife@udea.edu.co",
                        url = "https://telconova.com"
                ),
                license = @License(
                        name = "Propietario",
                        url = "https://telconova.com/licencia"
                )
        ),
        servers = {
                @Server(
                        description = "Servidor de Desarrollo",
                        url = "http://localhost:8080"
                ),
                @Server(
                        description = "Servidor de Pruebas",
                        url = "https://api-test.telconova.com"
                ),
                @Server(
                        description = "Servidor de Producción",
                        url = "https://api.telconova.com"
                )
        }
)
public class TelconovaSupportsuiteApplication {

    public static void main(String[] args) {
        System.setProperty("spring.application.name", "TelcoNova SupportSuite");

        System.setProperty("user.timezone", "America/Bogota");

        System.setProperty("file.encoding", "UTF-8");

        SpringApplication app = new SpringApplication(TelconovaSupportsuiteApplication.class);

        app.setBannerMode(org.springframework.boot.Banner.Mode.CONSOLE);

        app.run(args);

        log.info("""
            
            ╔═══════════════════════════════════════════════════════════════╗
            ║              TelcoNova SupportSuite - INICIADO                ║
            ║                                                               ║
            ║  🚀 Feature 3: Seguimiento de Órdenes en Proceso              ║
            ║  📚 Documentación: http://localhost:8080/swagger-ui.html      ║
            ║  📊 Métricas: http://localhost:8080/actuator/prometheus       ║
            ║  💊 Health: http://localhost:8080/actuator/health             ║
            ║                                                               ║
            ║         Desarrollado con ❤️ por el equipo de U de A           ║
            ╚═══════════════════════════════════════════════════════════════╝
            """);
    }
}

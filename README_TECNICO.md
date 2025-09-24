🔧 TelcoNova SupportSuite - Documentación Técnica
📋 Información General
AtributoValorProyectoTelcoNova SupportSuiteFeature3 - Seguimiento de Órdenes en ProcesoVersión1.0.0Java21 LTSSpring Boot3.2.0Base de DatosPostgreSQL 15+ArquitecturaHexagonal (Ports & Adapters)Gestión DependenciasMaven 3.8+

🏗️ Arquitectura del Sistema
Arquitectura Hexagonal
┌─────────────────────────────────────────────────────────────┐
│                    ADAPTADORES PRIMARIOS                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │ REST API    │  │   Swagger   │  │  Métricas   │         │
│  │Controllers  │  │    Docs     │  │ Actuator    │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
└─────────────────────────────────────────────────────────────┘
│
▼
┌─────────────────────────────────────────────────────────────┐
│                    CAPA DE APLICACIÓN                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │   Use Cases │  │    DTOs     │  │   Puertos   │         │
│  │  Services   │  │ Req/Resp    │  │ Interfaces  │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
└─────────────────────────────────────────────────────────────┘
│
▼
┌─────────────────────────────────────────────────────────────┐
│                      DOMINIO CORE                           │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │ Entidades   │  │    Enums    │  │ Value Objs  │         │
│  │   Domain    │  │   Estados   │  │   Email     │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
└─────────────────────────────────────────────────────────────┘
│
▼
┌─────────────────────────────────────────────────────────────┐
│                  ADAPTADORES SECUNDARIOS                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │ PostgreSQL  │  │ File Storage│  │   Security  │         │
│  │ Repository  │  │   S3/Local  │  │    JWT      │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
└─────────────────────────────────────────────────────────────┘
Principios SOLID Aplicados

Single Responsibility: Cada clase tiene una única responsabilidad
Open/Closed: Extensible sin modificar código existente
Liskov Substitution: Las implementaciones son intercambiables
Interface Segregation: Interfaces específicas y cohesivas
Dependency Inversion: Dependencias hacia abstracciones


🗄️ Modelo de Datos
Diagrama MER
mermaiderDiagram
USUARIO {
bigint id PK
varchar email UK
varchar contrasena
varchar nombre_completo
varchar rol
boolean activo
timestamp fecha_creacion
}

    ORDEN_TRABAJO {
        bigint id PK
        varchar numero_orden UK
        varchar titulo
        varchar estado
        varchar prioridad
        bigint tecnico_asignado_id FK
        timestamp fecha_asignacion
        timestamp fecha_inicio_trabajo
        timestamp fecha_fin_trabajo
    }
    
    EVIDENCIA {
        bigint id PK
        bigint orden_trabajo_id FK
        varchar tipo
        text contenido
        varchar ruta_archivo
        timestamp fecha_creacion
        bigint creado_por FK
    }
    
    MATERIAL {
        bigint id PK
        varchar codigo UK
        varchar nombre
        decimal precio_unitario
        integer stock_disponible
        boolean activo
    }
    
    MATERIAL_UTILIZADO {
        bigint id PK
        bigint orden_trabajo_id FK
        bigint material_id FK
        integer cantidad_utilizada
        timestamp fecha_registro
        bigint registrado_por FK
    }
    
    USUARIO ||--o{ ORDEN_TRABAJO : "asignado_a"
    ORDEN_TRABAJO ||--o{ EVIDENCIA : "tiene"
    ORDEN_TRABAJO ||--o{ MATERIAL_UTILIZADO : "consume"
    MATERIAL ||--o{ MATERIAL_UTILIZADO : "referenciado_en"
Índices de Performance
sql-- Índices críticos para rendimiento
CREATE INDEX idx_ordenes_tecnico_estado ON ordenes_trabajo(tecnico_asignado_id, estado);
CREATE INDEX idx_evidencias_orden_fecha ON evidencias(orden_trabajo_id, fecha_creacion);
CREATE INDEX idx_materiales_nombre_activo ON materiales(nombre, activo);
CREATE INDEX idx_usuarios_email_activo ON usuarios(email, activo);

🔐 Seguridad Implementada
Autenticación JWT
java// Configuración JWT
jwt:
secret-key: ${JWT_SECRET}
expiration: 86400000  # 24 horas
refresh-expiration: 604800000  # 7 días
Validaciones de Seguridad

Email Domain Validation: Solo emails @telconova.com
Password Encryption: BCrypt con salt rounds = 12
JWT Validation: Verificación de firma y expiración
Role-Based Access: TECNICO vs ADMIN permissions
File Upload Security: Validación de tipos MIME y tamaños

Headers de Seguridad
yamlsecurity:
headers:
frame-options: DENY
content-type-options: nosniff
xss-protection: "1; mode=block"
referrer-policy: no-referrer
csp: "default-src 'self'"

📊 API Endpoints
Autenticación
MétodoEndpointDescripciónAutenticaciónPOST/auth/loginIniciar sesiónNoPOST/auth/refreshRenovar tokenJWTPOST/auth/logoutCerrar sesiónJWT
Órdenes de Trabajo
MétodoEndpointDescripciónRolGET/ordenes/mis-ordenesÓrdenes del técnicoTECNICOGET/ordenes/todasTodas las órdenesADMINGET/ordenes/{id}Detalle de ordenTECNICO/ADMINPUT/ordenes/{id}/estadoActualizar estadoTECNICOPOST/ordenes/{id}/finalizarFinalizar ordenTECNICO
Evidencias
MétodoEndpointDescripciónRolPOST/ordenes/{id}/evidencias/comentarioAgregar comentarioTECNICOPOST/ordenes/{id}/evidencias/fotoSubir fotoTECNICOGET/ordenes/{id}/evidenciasListar evidenciasTECNICO/ADMINDELETE/evidencias/{id}Eliminar evidenciaTECNICO/ADMIN
Materiales
MétodoEndpointDescripciónRolGET/materiales/buscar?q={query}Buscar materialesTECNICOPOST/ordenes/{id}/materialesAgregar materialTECNICOGET/materialesListar materialesADMINPOST/materialesCrear materialADMIN

🧪 Testing Strategy
Pirámide de Testing
/\
/  \
/ E2E \     ← 10% - Tests End-to-End
/______\
/        \
/Integration\   ← 20% - Tests de Integración
/__________\
/            \
/    Unit      \   ← 70% - Tests Unitarios
/______________\
Cobertura de Testing

Unit Tests: 85%+ cobertura
Integration Tests: Componentes críticos
E2E Tests: Flujos principales de usuario
Security Tests: OWASP ZAP scanning
Performance Tests: JMeter load testing

Comandos de Testing
bash# Tests unitarios
mvn test

# Tests de integración
mvn integration-test

# Tests con cobertura
mvn jacoco:prepare-agent test jacoco:report

# Tests de seguridad
mvn dependency-check:check

📈 Métricas y Monitoreo
Métricas Expuestas
Métricas de Aplicación
yaml# Custom metrics
telconova.ordenes.activas: Gauge
telconova.ordenes.finalizadas.total: Counter
telconova.materiales.utilizados.total: Counter
telconova.usuarios.activos: Gauge
telconova.evidencias.subidas.total: Counter
Métricas de Sistema
yaml# Spring Boot Actuator
jvm.memory.used
jvm.threads.live
http.server.requests
system.cpu.usage
Dashboards Grafana

Dashboard Operacional

Órdenes por estado
Tiempo promedio de resolución
Técnicos más activos
Materiales más utilizados


Dashboard Técnico

Métricas de JVM
Response time por endpoint
Error rates
Database connections


Dashboard de Negocio

KPIs de productividad
Costo de materiales
Satisfaction scores
Tendencias mensuales




🚀 Despliegue
Entornos
bash# Desarrollo
mvn spring-boot:run -Dspring.profiles.active=dev

# Testing
mvn spring-boot:run -Dspring.profiles.active=test

# Producción
java -jar -Dspring.profiles.active=prod app.jar
Variables de Entorno Críticas
bash# Base de datos
DB_HOST=localhost
DB_PORT=5432
DB_NAME=telconova_db
DB_USER=telconova_app
DB_PASSWORD=${DB_PASSWORD}

# JWT Security
JWT_SECRET=${JWT_SECRET_KEY}
JWT_EXPIRATION=86400000

# File Storage
STORAGE_TYPE=s3
S3_BUCKET_NAME=${BUCKET_NAME}
S3_ACCESS_KEY=${AWS_ACCESS_KEY}
S3_SECRET_KEY=${AWS_SECRET_KEY}
Docker Deployment
bash# Build y push
docker build -t telconova/supportsuite:1.0.0 .
docker push telconova/supportsuite:1.0.0

# Deploy con docker-compose
docker-compose up -d

# Scaling
docker-compose up --scale app=3

🔍 Troubleshooting
Logs Estructurados
json{
"timestamp": "2025-01-15T10:30:00.000Z",
"level": "INFO",
"thread": "http-nio-8080-exec-1",
"logger": "com.telconova.supportsuite.aplicacion.servicios.OrdenTrabajoService",
"message": "Orden 123 actualizada a estado EN_PROCESO",
"mdc": {
"userId": "juan.perez@telconova.com",
"orderId": "123",
"requestId": "abc-def-ghi"
}
}
Health Checks
bash# Application health
curl http://localhost:8080/api/v1/actuator/health

# Database connectivity
curl http://localhost:8080/api/v1/actuator/health/db

# Disk space
curl http://localhost:8080/api/v1/actuator/health/diskSpace
Common Issues
IssueCausaSoluciónJWT InvalidToken expiradoRenovar token con /auth/refreshFile Upload ErrorTipo no permitidoVerificar MIME type: JPG, PNGDatabase ConnectionPool exhaustedVerificar hikari settingsPermission DeniedRol insuficienteVerificar TECNICO vs ADMIN

🔧 Configuración de Desarrollo
IDE Setup
IntelliJ IDEA
xml<!-- .idea/compiler.xml -->
<component name="CompilerConfiguration">
<annotationProcessing>
<profile name="Maven default">
<sourceOutputDir name="target/generated-sources/annotations" />
<processor name="lombok.launch.AnnotationProcessorHider$AnnotationProcessor" />
<processor name="org.mapstruct.ap.MappingProcessor" />
</profile>
</annotationProcessing>
</component>
VS Code Extensions
json{
"recommendations": [
"vscjava.vscode-java-pack",
"pivotal.vscode-boot-dev-pack",
"redhat.vscode-yaml",
"ms-vscode.vscode-json"
]
}
Pre-commit Hooks
bash#!/bin/sh
# .git/hooks/pre-commit
mvn checkstyle:check
mvn compile
mvn test

📚 Referencias Técnicas
Documentación Externa

Spring Boot Reference
PostgreSQL Documentation
JWT.io
Docker Documentation
Prometheus Documentation

Arquitectura Hexagonal

Ports and Adapters Pattern
Domain Driven Design

Seguridad

OWASP Top 10
JWT Security Best Practices


👥 Equipo de Desarrollo
RolResponsabilidadTech LeadArquitectura y decisiones técnicasBackend DeveloperImplementación de serviciosDevOps EngineerCI/CD y infraestructuraQA EngineerTesting y calidadSecurity EngineerAuditoría de seguridad

📞 Contacto Técnico

Email: desarrollo@telconova.com
Slack: #telconova-dev
JIRA: TCNV Project
Confluence: Technical Docs
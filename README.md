# Backend
Microservicio seguro para la gestión y trazabilidad de órdenes de trabajo. Maneja autenticación de técnicos, registro de avances, actualización de estados, generación de notificaciones y provee datos al dashboard de seguimiento cumpliendo estándares de seguridad y accesibilidad.

# 📱 TelcoNova SupportSuite - Manual de Usuario

<div align="center">

![TelcoNova Logo](https://via.placeholder.com/200x80/0066CC/FFFFFF?text=TelcoNova)

**Sistema de Gestión de Soporte Técnico**  
*Feature 3: Seguimiento de Órdenes en Proceso*

[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](https://github.com/telconova/supportsuite)
[![Java](https://img.shields.io/badge/java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/spring%20boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/postgresql-15-blue.svg)](https://postgresql.org/)
[![License](https://img.shields.io/badge/license-Proprietario-red.svg)](LICENSE)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=AndresT3086_BackendTelconova&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=AndresT3086_BackendTelconova)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=AndresT3086_BackendTelconova&metric=coverage)](https://sonarcloud.io/summary/new_code?id=AndresT3086_BackendTelconova)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=AndresT3086_BackendTelconova&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=AndresT3086_BackendTelconova)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=AndresT3086_BackendTelconova&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=AndresT3086_BackendTelconova)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=AndresT3086_BackendTelconova&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=AndresT3086_BackendTelconova)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=AndresT3086_BackendTelconova&metric=bugs)](https://sonarcloud.io/summary/new_code?id=AndresT3086_BackendTelconova)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=AndresT3086_BackendTelconova&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=AndresT3086_BackendTelconova)
</div>

---

## 📋 Descripción General

**TelcoNova SupportSuite** es un sistema integral de gestión de soporte técnico diseñado específicamente para optimizar el seguimiento y control de órdenes de trabajo en campo. Este módulo permite a los técnicos registrar avances, documentar evidencias y gestionar materiales utilizados durante sus intervenciones.

### 🎯 Funcionalidades Principales

- **🔐 Autenticación Segura**: Login con credenciales @telconova.com
- **📋 Gestión de Órdenes**: Visualización y actualización de órdenes asignadas
- **📸 Registro de Evidencias**: Carga de comentarios y fotos
- **🔧 Control de Materiales**: Registro de materiales utilizados
- **📊 Dashboard Administrativo**: Vista completa para supervisores
- **📱 API REST**: Integración con aplicaciones móviles

---

## 👥 Roles de Usuario

### 🔧 TÉCNICO
- Ver órdenes asignadas personalmente
- Actualizar estado de órdenes (EN_PROCESO, PAUSADA, FINALIZADA)
- Agregar evidencias (comentarios y fotos)
- Registrar materiales utilizados
- Finalizar órdenes con tiempos de trabajo

### 👨‍💼 ADMINISTRADOR
- Ver todas las órdenes del sistema
- Acceso a dashboard completo
- Gestión de materiales y usuarios
- Reportes y métricas avanzadas

---

## 🚀 Guía de Inicio Rápido

### 📋 Prerrequisitos

- **Java 21** o superior
- **PostgreSQL 15** o superior
- **Maven 3.8** o superior
- **Docker** (opcional, para despliegue con contenedores)

### ⬇️ Instalación

#### Opción 1: Instalación Manual

```bash
# 1. Clonar el repositorio
git clone https://github.com/TelcoNova-SupportSuite-F3/Backend
cd supportsuite

# 2. Configurar base de datos
createdb telconova_db
psql -d telconova_db -f scripts/database/schema_creation.sql

# 3. Configurar variables de entorno
export DB_HOST=localhost
export DB_PASSWORD=TelcoN0v4_2025!
export JWT_SECRET=secret-key

# 4. Compilar y ejecutar
mvn clean install
mvn spring-boot:run
```

#### Opción 2: Despliegue con Docker

```bash
# 1. Clonar repositorio
git clone https://github.com/TelcoNova-SupportSuite-F3/Backend
cd supportsuite

# 2. Levantar servicios
docker-compose up -d

# 3. Verificar estado
docker-compose ps
```

### ✅ Verificación de Instalación

Una vez iniciado el sistema, verifica que esté funcionando correctamente:

```bash
# Health Check
curl http://localhost:8080/api/v1/actuator/health

# Respuesta esperada:
# {"status":"UP","components":{"db":{"status":"UP"},"diskSpace":{"status":"UP"}}}
```

### 🌐 Acceder al Sistema

- **API Base URL**: `http://localhost:8080/api/v1`
- **Documentación Swagger**: `http://localhost:8080/swagger-ui/index.html`
- **Métricas**: `http://localhost:8080/api/v1/actuator/prometheus`
- **Grafana** (si usar Docker): `http://localhost:3000` (admin/TelcoNova2025!)

---

## 📱 Guía de Uso

### 1. 🔐 Iniciar Sesión

**Endpoint**: `POST /auth/login`

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "juan.perez@telconova.com",
    "contrasena": "miContrasena123"
  }'
```

**Respuesta**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tipoToken": "Bearer",
  "email": "juan.perez@telconova.com",
  "nombreCompleto": "Juan Pérez González",
  "rol": "TECNICO",
  "expiracion": "2025-01-16T10:30:00",
  "activo": true
}
```

⚠️ **Importante**: Guarda el token, lo necesitarás para todas las demás operaciones.

### 2. 📋 Ver Mis Órdenes (TÉCNICO)

**Endpoint**: `GET /ordenes/mis-ordenes`

```bash
curl -X GET http://localhost:8080/api/v1/ordenes/mis-ordenes \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Respuesta**:
```json
[
  {
    "id": 1,
    "numeroOrden": "ORD-2025-001",
    "titulo": "Instalación Internet Residencial",
    "estado": "ASIGNADA",
    "prioridad": "MEDIA",
    "clienteNombre": "Ana García Ruiz",
    "direccion": "Carrera 70 #45-23, Medellín",
    "fechaAsignacion": "2025-01-15T08:00:00",
    "estaVencida": false
  }
]
```

### 3. 📋 Ver Todas las Órdenes (ADMIN)

**Endpoint**: `GET /ordenes/todas`

```bash
curl -X GET http://localhost:8080/api/v1/ordenes/todas \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

### 4. 🔄 Actualizar Estado de Orden

**Endpoint**: `PUT /ordenes/{id}/estado`

```bash
# Iniciar trabajo
curl -X PUT http://localhost:8080/api/v1/ordenes/1/estado \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nuevoEstado": "EN_PROCESO",
    "observaciones": "Iniciando trabajo en sitio del cliente"
  }'
```

### 5. 💬 Agregar Comentario

**Endpoint**: `POST /ordenes/{id}/evidencias/comentario`

```bash
curl -X POST http://localhost:8080/api/v1/ordenes/1/evidencias/comentario \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "comentario": "Instalación del módem completada. Cliente conforme con la velocidad."
  }'
```

### 6. 📸 Subir Foto

**Endpoint**: `POST /ordenes/{id}/evidencias/foto`

```bash
curl -X POST http://localhost:8080/api/v1/ordenes/1/evidencias/foto \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "foto=@/ruta/a/tu/imagen.jpg"
```

### 7. 🔧 Agregar Material

**Endpoint**: `POST /ordenes/{id}/materiales`

```bash
curl -X POST http://localhost:8080/api/v1/ordenes/1/materiales \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "materialId": 5,
    "cantidad": 1
  }'
```

### 8. 🔍 Buscar Materiales

**Endpoint**: `GET /materiales/buscar?q={query}&limite={limite}`

```bash
curl -X GET "http://localhost:8080/api/v1/materiales/buscar?q=cable&limite=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Respuesta**:
```json
[
  {
    "id": 1,
    "codigo": "CAB-UTP-001",
    "nombre": "Cable UTP Categoría 6",
    "descripcion": "Cable de red UTP Cat 6",
    "unidadMedida": "metros",
    "precioUnitario": 2.50,
    "stockDisponible": 1000,
    "activo": true
  }
]
```

### 9. ✅ Finalizar Orden

**Endpoint**: `POST /ordenes/{id}/finalizar`

```bash
curl -X POST http://localhost:8080/api/v1/ordenes/1/finalizar \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nuevoEstado": "FINALIZADA",
    "fechaInicioTrabajo": "2025-01-15T08:30:00",
    "fechaFinTrabajo": "2025-01-15T12:45:00",
    "observaciones": "Instalación completada exitosamente. Cliente satisfecho."
  }'
```

⚠️ **Importante**: Para finalizar una orden se requiere:
- Fechas de inicio y fin obligatorias
- Al menos un comentario o foto como evidencia
- Estado actual EN_PROCESO o PAUSADA

---

## 📊 Estados de Órdenes

| Estado | Descripción | Acciones Permitidas |
|--------|-------------|---------------------|
| **ASIGNADA** | Orden asignada al técnico | ▶️ Iniciar trabajo |
| **EN_PROCESO** | Técnico trabajando | ⏸️ Pausar, ✅ Finalizar, 🔧 Agregar materiales |
| **PAUSADA** | Trabajo temporalmente detenido | ▶️ Reanudar, ✅ Finalizar |
| **FINALIZADA** | Trabajo completado | ❌ Sin acciones (solo lectura) |

---

## 📸 Gestión de Archivos

### Tipos de Archivo Permitidos
- **JPG/JPEG** (máximo 10MB)
- **PNG** (máximo 10MB)

### Limitaciones
- **Comentarios**: Máximo 500 caracteres
- **Archivos**: Máximo 10MB por archivo
- **Formatos**: Solo imágenes (JPG, JPEG, PNG)

### Almacenamiento
- **Desarrollo**: Almacenamiento local en `./uploads`
- **Producción**: Amazon S3 (configurado vía variables de entorno)

---

## 🔐 Reglas de Seguridad

### Autenticación
- Solo emails del dominio **@telconova.com**
- Tokens JWT con expiración de **24 horas**
- Contraseñas encriptadas con **BCrypt**

### Autorización
- **Técnicos**: Solo ven sus órdenes asignadas
- **Administradores**: Ven todas las órdenes del sistema
- **Validaciones**: Verificación de permisos en cada endpoint

### Validaciones de Negocio
- ✅ Solo materiales **EN_PROCESO** permiten agregar materiales
- ✅ Solo órdenes **EN_PROCESO/PAUSADA** se pueden finalizar
- ✅ Fechas de inicio y fin **obligatorias** para finalización
- ✅ Al menos **una evidencia** requerida para finalizar

---

## 🛠️ Configuración

### Variables de Entorno

```bash
# Base de datos
DB_HOST=localhost
DB_PORT=5432
DB_NAME=telconova_db
DB_USER=telconova_app
DB_PASSWORD=TelcoN0v4_2025!

# Seguridad JWT
JWT_SECRET=TelcoNova2025_SupportSuite_SecretKey_MuySeguro
JWT_EXPIRATION=86400000

# Almacenamiento
STORAGE_TYPE=local                    # local | s3
LOCAL_UPLOAD_DIR=./uploads
S3_BUCKET_NAME=telconova-evidence     # Solo si STORAGE_TYPE=s3
S3_REGION=us-east-1                   # Solo si STORAGE_TYPE=s3

# CORS
CORS_ORIGINS=http://localhost:3000,http://localhost:4200
```

### Profiles de Spring

```bash
# Desarrollo
mvn spring-boot:run -Dspring.profiles.active=dev

# Testing  
mvn spring-boot:run -Dspring.profiles.active=test

# Producción
java -jar app.jar --spring.profiles.active=prod
```

---

## 📊 Monitoreo y Métricas

### Health Checks

| Endpoint | Descripción |
|----------|-------------|
| `/actuator/health` | Estado general de la aplicación |
| `/actuator/health/db` | Conectividad con base de datos |
| `/actuator/info` | Información de la aplicación |

### Métricas Prometheus

```bash
# Obtener métricas
curl http://localhost:8080/api/v1/actuator/prometheus

# Métricas principales:
# - telconova_ordenes_activas_total
# - telconova_usuarios_conectados_total  
# - telconova_evidencias_subidas_total
# - http_server_requests_seconds
# - jvm_memory_used_bytes
```

### Dashboard Grafana

Si usas Docker Compose, Grafana estará disponible en `http://localhost:3000`:

- **Usuario**: admin
- **Contraseña**: TelcoNova2025!

**Dashboards Incluidos**:
- 📊 **Operacional**: Órdenes, técnicos, materiales
- 🔧 **Técnico**: JVM, response times, errors
- 📈 **Negocio**: KPIs, productividad, costos

---

## 🐳 Docker Deployment

### Despliegue Simple

```bash
# 1. Clonar repositorio
git clone https://github.com/telconova/supportsuite.git
cd supportsuite

# 2. Configurar variables (opcional)
cp .env.example .env
# Editar .env con tus configuraciones

# 3. Levantar servicios
docker-compose up -d

# 4. Verificar logs
docker-compose logs -f app

# 5. Ver estado de servicios
docker-compose ps
```

### Servicios Incluidos

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| **app** | 8080 | Aplicación Spring Boot |
| **postgres** | 5432 | Base de datos PostgreSQL |
| **nginx** | 80/443 | Proxy reverso y SSL |
| **prometheus** | 9090 | Recolector de métricas |
| **grafana** | 3000 | Visualización de dashboards |
| **redis** | 6379 | Cache (futuro uso) |

### Comandos Útiles

```bash
# Ver logs en tiempo real
docker-compose logs -f app

# Reiniciar solo la aplicación
docker-compose restart app

# Escalar aplicación (múltiples instancias)
docker-compose up -d --scale app=3

# Backup de base de datos
docker-compose exec postgres pg_dump -U telconova_app telconova_db > backup.sql

# Restaurar base de datos
docker-compose exec -T postgres psql -U telconova_app telconova_db < backup.sql

# Parar todos los servicios
docker-compose down

# Parar y limpiar volúmenes
docker-compose down -v
```

---

## 🚨 Solución de Problemas

### Problemas Comunes

#### 🔴 Error: "JWT Token Invalid"

**Síntomas**: HTTP 401 en las peticiones  
**Causa**: Token expirado o inválido  
**Solución**:
```bash
# Hacer login nuevamente
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"tu-email@telconova.com","contrasena":"tu-contraseña"}'
```

#### 🔴 Error: "File Upload Failed"

**Síntomas**: Error 400 al subir fotos  
**Causas Posibles**:
- Archivo muy grande (>10MB)
- Tipo no permitido (solo JPG, PNG)
- Falta de permisos en directorio uploads

**Solución**:
```bash
# Verificar permisos del directorio
chmod 755 uploads/

# Verificar tipo y tamaño del archivo
file tu-imagen.jpg
ls -lh tu-imagen.jpg
```

#### 🔴 Error: "Cannot add materials to order"

**Síntomas**: Error 400 al agregar materiales  
**Causa**: La orden no está en estado EN_PROCESO  
**Solución**:
```bash
# Primero cambiar estado de la orden
curl -X PUT http://localhost:8080/api/v1/ordenes/1/estado \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"nuevoEstado":"EN_PROCESO"}'
```

#### 🔴 Error: "Database Connection Failed"

**Síntomas**: Error 500, aplicación no inicia  
**Causa**: PostgreSQL no disponible o credenciales incorrectas  
**Solución**:
```bash
# Verificar que PostgreSQL esté corriendo
systemctl status postgresql

# Probar conexión manual
psql -h localhost -U telconova_app -d telconova_db

# Si usas Docker
docker-compose logs postgres
```

### Logs de Aplicación

```bash
# Ver logs en tiempo real
tail -f logs/telconova-supportsuite.log

# Filtrar errores
grep ERROR logs/telconova-supportsuite.log

# Filtrar por usuario específico
grep "juan.perez@telconova.com" logs/telconova-supportsuite.log
```

### Validación de Configuración

```bash
# Verificar configuración actual
curl http://localhost:8080/api/v1/actuator/configprops

# Verificar variables de entorno
curl http://localhost:8080/api/v1/actuator/env
```

---

## 📞 Soporte y Contacto

### 🆘 Soporte Técnico

- **Email**: soporte@telconova.com
- **Teléfono**: +57 4 444-1234
- **Horarios**: Lunes a Viernes, 8:00 AM - 6:00 PM

### 👨‍💻 Equipo de Desarrollo

- **Email**: desarrollo@telconova.com
- **Slack**: #telconova-dev
- **JIRA**: [Reportar Bug](https://telconova.atlassian.net)

### 📚 Recursos Adicionales

- **Documentación API**: `/swagger-ui.html`
- **Confluence**: [Wiki Técnica](https://telconova.atlassian.net/wiki)
- **Código Fuente**: [GitHub](https://github.com/telconova/supportsuite)

---

## 📜 Changelog

### v1.0.0 (2025-01-15)

#### ✨ Nuevas Funcionalidades
- 🔐 Sistema de autenticación JWT con validación @telconova.com
- 📋 Gestión completa de órdenes de trabajo
- 📸 Registro de evidencias (comentarios y fotos)
- 🔧 Control de materiales utilizados
- 👨‍💼 Dashboard administrativo
- 📊 Métricas y monitoreo con Prometheus/Grafana
- 🐳 Despliegue con Docker Compose

#### 🔒 Seguridad
- Encriptación de contraseñas con BCrypt
- Validación de tokens JWT
- Control de acceso basado en roles
- Validación de tipos de archivo
- Headers de seguridad HTTP

#### 📈 Performance
- Índices optimizados en PostgreSQL
- Connection pooling con HikariCP
- Cache con Redis (preparado)
- Compresión de respuestas HTTP

---

## 📄 Licencia

Este software es propiedad de **TelcoNova Colombia SAS**. Todos los derechos reservados.

El uso de este software está restringido a empleados autorizados de TelcoNova Colombia y está sujeto a los términos del contrato de licencia corporativo.

---

<div align="center">

**🚀 ¡Gracias por usar TelcoNova SupportSuite! 🚀**

Si tienes preguntas o necesitas ayuda, no dudes en contactarnos.

---

*Desarrollado con ❤️ por el equipo de TelcoNova Colombia*

</div>
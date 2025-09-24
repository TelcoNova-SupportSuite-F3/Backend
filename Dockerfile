# =====================================================
# ETAPA DE CONSTRUCCIÓN
# =====================================================
FROM eclipse-temurin:21-jdk-slim as builder

# Información del mantenedor
LABEL maintainer="desarrollo@telconova.com"
LABEL version="1.0.0"
LABEL description="TelcoNova SupportSuite - Sistema de Gestión de Soporte Técnico"

# Instalar Maven
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración de Maven primero (para cache de capas)
COPY pom.xml .

# Descargar dependencias (se cachea si pom.xml no cambia)
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Construir la aplicación
RUN mvn clean package -DskipTests

# =====================================================
# IMAGEN FINAL
# =====================================================
FROM eclipse-temurin:21-jre-slim

# Crear usuario no-root para seguridad
RUN groupadd -r telconova && useradd -r -g telconova telconova

# Crear directorios necesarios
RUN mkdir -p /app/logs /app/uploads && \
    chown -R telconova:telconova /app

# Instalar herramientas útiles para troubleshooting
RUN apt-get update && \
    apt-get install -y curl wget netcat-openbsd && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Establecer directorio de trabajo
WORKDIR /app

# Copiar JAR desde la etapa de construcción
COPY --from=builder /app/target/*.jar app.jar

# Cambiar permisos
RUN chown telconova:telconova app.jar

# Cambiar a usuario no-root
USER telconova

# Configurar variables de entorno
ENV JAVA_OPTS="-Xmx512m -Xms256m" \
    SPRING_PROFILES_ACTIVE=prod \
    TZ=America/Bogota

# Exponer puerto
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/v1/actuator/health || exit 1

# Punto de entrada
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
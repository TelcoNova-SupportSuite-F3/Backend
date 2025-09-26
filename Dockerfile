# Etapa de construcción
FROM maven:3.9.5-openjdk-21 AS build
WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .

# Descargar dependencias (se cachea si pom.xml no cambia)
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Construir la aplicación
RUN mvn clean package -DskipTests -B

# Etapa de ejecución
FROM openjdk:21-jdk-slim
WORKDIR /app

# Instalar curl para healthchecks
RUN apt-get update && \
    apt-get install -y curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Crear usuario no-root para seguridad
RUN groupadd -r telconova && useradd -r -g telconova telconova

# Crear directorios necesarios
RUN mkdir -p /app/logs /app/uploads && \
    chown -R telconova:telconova /app

# Copiar el JAR construido
COPY --from=build /app/target/supportsuite-1.0.0-SNAPSHOT.jar app.jar

# Cambiar propietario del JAR
RUN chown telconova:telconova app.jar

# Cambiar al usuario no-root
USER telconova

# Exponer puerto
EXPOSE 8080

# Variables de entorno por defecto
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
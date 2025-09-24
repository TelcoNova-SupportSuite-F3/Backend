package com.telconova.supportsuite.infraestructura.adaptadores.salida.almacenamiento;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.telconova.supportsuite.aplicacion.puertos.salida.IAlmacenamientoArchivos;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.telconova.supportsuite.compartido.constantes.ConfiguracionConstantes;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

@Slf4j
@Service
@Primary
public class CloudinaryAlmacenamientoImpl implements IAlmacenamientoArchivos {

    private final Cloudinary cloudinary;
    private final String carpetaBase;

    public CloudinaryAlmacenamientoImpl(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret,
            @Value("${cloudinary.folder:telconova-evidencias}") String carpetaBase) {

        this.carpetaBase = carpetaBase;
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true));

        log.info("Cloudinary configurado para cloud: {} en carpeta: {}", cloudName, carpetaBase);
    }

    @Override
    public String guardarArchivo(MultipartFile archivo, String carpeta) {
        log.info("Guardando archivo en Cloudinary: {} en carpeta: {}",
                archivo.getOriginalFilename(), carpeta);

        // Validar archivo antes de subir
        validarArchivo(archivo);

        try {
            // Configurar opciones de subida
            Map<String, Object> opciones = ObjectUtils.asMap(
                    "folder", carpetaBase + "/" + carpeta,
                    "resource_type", "image",
                    "use_filename", true,
                    "unique_filename", true,
                    "overwrite", false,
                    "quality", "auto",
                    "fetch_format", "auto"
            );

            // Subir archivo
            Map<String, Object> resultado = cloudinary.uploader()
                    .upload(archivo.getBytes(), opciones);

            String publicId = (String) resultado.get("public_id");
            String url = (String) resultado.get("secure_url");
            Long bytes = ((Number) resultado.get("bytes")).longValue();

            log.info("Archivo guardado exitosamente en Cloudinary. Public ID: {}, URL: {}, Tamaño: {} bytes",
                    publicId, url, bytes);

            return publicId;

        } catch (IOException e) {
            log.error("Error guardando archivo en Cloudinary: {}", archivo.getOriginalFilename(), e);
            throw new RuntimeException("Error al guardar archivo en Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminarArchivo(String rutaArchivo) {
        log.info("Eliminando archivo de Cloudinary: {}", rutaArchivo);

        try {
            Map<String, Object> resultado = cloudinary.uploader()
                    .destroy(rutaArchivo, ObjectUtils.emptyMap());

            String status = (String) resultado.get("result");
            if ("ok".equals(status)) {
                log.info("Archivo eliminado exitosamente de Cloudinary: {}", rutaArchivo);
            } else if ("not found".equals(status)) {
                log.warn("Archivo no encontrado en Cloudinary para eliminar: {}", rutaArchivo);
            } else {
                log.warn("Estado inesperado al eliminar archivo de Cloudinary: {}", status);
            }

        } catch (IOException e) {
            log.error("Error eliminando archivo de Cloudinary: {}", rutaArchivo, e);
            throw new RuntimeException("Error al eliminar archivo de Cloudinary", e);
        }
    }

    @Override
    public boolean archivoExiste(String rutaArchivo) {
        try {
            Map<String, Object> resultado = cloudinary.api()
                    .resource(rutaArchivo, ObjectUtils.emptyMap());
            return resultado != null && "image".equals(resultado.get("resource_type"));
        } catch (Exception e) {
            log.debug("Archivo no encontrado en Cloudinary: {}", rutaArchivo);
            return false;
        }
    }

    @Override
    public String obtenerUrlPublica(String rutaArchivo) {
        if (rutaArchivo == null || rutaArchivo.trim().isEmpty()) {
            return null;
        }

        // Generar URL optimizada para web con transformaciones automáticas
        return cloudinary.url()
                .transformation(new com.cloudinary.Transformation()
                        .quality("auto")
                        .fetchFormat("auto")
                        .dpr("auto"))
                .secure(true)
                .generate(rutaArchivo);
    }

    @Override
    public byte[] obtenerContenidoArchivo(String rutaArchivo) {
        // Para Cloudinary no necesitamos esto ya que las imágenes se acceden directamente por URL
        // Retornamos array vacío ya que el contenido se obtiene a través de la URL pública
        log.info("obtenerContenidoArchivo llamado para Cloudinary. Usar obtenerUrlPublica() en su lugar");
        return new byte[0];
    }

    @Override
    public long obtenerTamanoArchivo(String rutaArchivo) {
        try {
            Map<String, Object> resultado = cloudinary.api()
                    .resource(rutaArchivo, ObjectUtils.emptyMap());

            Number bytes = (Number) resultado.get("bytes");
            return bytes != null ? bytes.longValue() : 0;

        } catch (Exception e) {
            log.error("Error obteniendo tamaño de archivo de Cloudinary: {}", rutaArchivo, e);
            return 0;
        }
    }

    @Override
    public boolean esTipoMimePermitido(String tipoMime) {
        if (tipoMime == null) {
            return false;
        }
        return Arrays.asList(ConfiguracionConstantes.TIPOS_MIME_PERMITIDOS).contains(tipoMime);
    }

    /**
     * Validación completa del archivo antes de subir
     */
    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }

        if (!esTipoMimePermitido(archivo.getContentType())) {
            throw new IllegalArgumentException("Tipo de archivo no permitido: " + archivo.getContentType() +
                    ". Solo se permiten: " + String.join(", ", ConfiguracionConstantes.TIPOS_MIME_PERMITIDOS));
        }

        if (archivo.getSize() > ConfiguracionConstantes.TAMAANO_MAXIMO_ARCHIVO) {
            throw new IllegalArgumentException("Archivo demasiado grande. Tamaño actual: " +
                    formatearTamano(archivo.getSize()) + ", Máximo permitido: " +
                    formatearTamano(ConfiguracionConstantes.TAMAANO_MAXIMO_ARCHIVO));
        }

        log.debug("Archivo validado correctamente: {} - {} - {}",
                archivo.getOriginalFilename(), archivo.getContentType(), formatearTamano(archivo.getSize()));
    }

    /**
     * Formatea el tamaño en bytes a una representación legible
     */
    private String formatearTamano(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    /**
     * Obtiene URL con transformaciones específicas (miniatura, redimensionado, etc.)
     */
    public String obtenerUrlConTransformacion(String publicId, int ancho, int alto) {
        return cloudinary.url()
                .transformation(new com.cloudinary.Transformation()
                        .width(ancho)
                        .height(alto)
                        .crop("fill")
                        .quality("auto")
                        .fetchFormat("auto"))
                .secure(true)
                .generate(publicId);
    }

    /**
     * Obtiene URL de miniatura (150x150)
     */
    public String obtenerMiniatura(String publicId) {
        return obtenerUrlConTransformacion(publicId, 150, 150);
    }
}

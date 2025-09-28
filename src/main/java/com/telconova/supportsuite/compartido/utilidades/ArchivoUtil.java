package com.telconova.supportsuite.compartido.utilidades;

import com.telconova.supportsuite.compartido.constantes.ConfiguracionConstantes;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;

/**
 * Utilidades para manejo de archivos
 */
public class ArchivoUtil {

    private ArchivoUtil() {
        // Utility class
    }

    /**
     * Valida si el tipo MIME es permitido
     */
    public static boolean esTipoMimePermitido(String tipoMime) {
        if (tipoMime == null) {
            return false;
        }
        return Arrays.asList(ConfiguracionConstantes.TIPOS_MIME_PERMITIDOS)
                .contains(tipoMime.toLowerCase());
    }

    /**
     * Valida el tamaño del archivo
     */
    public static boolean esTamanoValido(long tamano) {
        return tamano > 0 && tamano <= ConfiguracionConstantes.TAMANO_MAXIMO_ARCHIVO;
    }

    /**
     * Genera un nombre único para el archivo
     */
    public static String generarNombreUnico(String nombreOriginal) {
        String extension = FilenameUtils.getExtension(nombreOriginal);
        String uuid = UUID.randomUUID().toString();
        return extension.isEmpty() ? uuid : uuid + "." + extension;
    }

    /**
     * Obtiene la extensión del archivo
     */
    public static String obtenerExtension(String nombreArchivo) {
        return FilenameUtils.getExtension(nombreArchivo).toLowerCase();
    }

    /**
     * Convierte bytes a formato legible
     */
    public static String formatearTamano(long bytes) {
        return FileUtils.byteCountToDisplaySize(bytes);
    }

    /**
     * Crea directorio si no existe
     */
    public static void crearDirectorioSiNoExiste(String ruta) throws IOException {
        Path path = Paths.get(ruta);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    /**
     * Valida archivo completo
     */
    public static void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }

        if (!esTipoMimePermitido(archivo.getContentType())) {
            throw new IllegalArgumentException("Tipo de archivo no permitido: " + archivo.getContentType());
        }

        if (!esTamanoValido(archivo.getSize())) {
            throw new IllegalArgumentException("Tamaño de archivo no válido: " + formatearTamano(archivo.getSize()));
        }
    }

    /**
     * Elimina archivo si existe
     */
    public static boolean eliminarArchivo(String rutaArchivo) {
        try {
            File archivo = new File(rutaArchivo);
            if (archivo.exists()) {
                return archivo.delete();
            }
            return true; // Si no existe, consideramos exitoso
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica si el archivo existe
     */
    public static boolean archivoExiste(String rutaArchivo) {
        return new File(rutaArchivo).exists();
    }

    /**
     * Obtiene el tipo MIME desde el nombre del archivo
     */
    public static String obtenerTipoMime(String nombreArchivo) {
        try {
            return Files.probeContentType(Paths.get(nombreArchivo));
        } catch (Exception e) {
            return "application/octet-stream";
        }
    }
}

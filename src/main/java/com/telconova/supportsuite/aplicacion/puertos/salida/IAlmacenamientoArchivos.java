package com.telconova.supportsuite.aplicacion.puertos.salida;

import org.springframework.web.multipart.MultipartFile;

public interface IAlmacenamientoArchivos {

    /**
     * Guarda un archivo y retorna la ruta
     */
    String guardarArchivo(MultipartFile archivo, String carpeta);

    /**
     * Elimina un archivo
     */
    void eliminarArchivo(String rutaArchivo);

    /**
     * Verifica si un archivo existe
     */
    boolean archivoExiste(String rutaArchivo);

    /**
     * Obtiene la URL pública de un archivo
     */
    String obtenerUrlPublica(String rutaArchivo);

    /**
     * Obtiene el contenido de un archivo como bytes
     */
    byte[] obtenerContenidoArchivo(String rutaArchivo);

    /**
     * Obtiene el tamaño de un archivo
     */
    long obtenerTamanoArchivo(String rutaArchivo);

    /**
     * Valida el tipo MIME de un archivo
     */
    boolean esTipoMimePermitido(String tipoMime);
}

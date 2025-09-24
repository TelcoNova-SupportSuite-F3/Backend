package com.telconova.supportsuite.dominio.excepciones;

public class EvidenciaNoValidaExcepcion extends DominioExcepcion {

    public EvidenciaNoValidaExcepcion(String mensaje) {
        super(mensaje);
    }

    public EvidenciaNoValidaExcepcion(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public static EvidenciaNoValidaExcepcion comentarioVacio() {
        return new EvidenciaNoValidaExcepcion("El comentario no puede estar vacío");
    }

    public static EvidenciaNoValidaExcepcion comentarioMuyLargo(int longitudActual, int longitudMaxima) {
        return new EvidenciaNoValidaExcepcion(
                String.format("El comentario excede la longitud máxima permitida. " +
                        "Actual: %d caracteres, Máximo: %d caracteres", longitudActual, longitudMaxima)
        );
    }

    public static EvidenciaNoValidaExcepcion archivoVacio() {
        return new EvidenciaNoValidaExcepcion("El archivo no puede estar vacío");
    }

    public static EvidenciaNoValidaExcepcion tipoArchivoNoPermitido(String tipoMime) {
        return new EvidenciaNoValidaExcepcion(
                String.format("El tipo de archivo '%s' no está permitido. " +
                        "Solo se permiten imágenes: JPG, JPEG, PNG, SVG", tipoMime)
        );
    }

    public static EvidenciaNoValidaExcepcion archivoMuyGrande(long tamaanoActual, long tamaanoMaximo) {
        return new EvidenciaNoValidaExcepcion(
                String.format("El archivo es demasiado grande. " +
                        "Tamaño actual: %d bytes, Tamaño máximo: %d bytes", tamaanoActual, tamaanoMaximo)
        );
    }

    public static EvidenciaNoValidaExcepcion evidenciaRequerida() {
        return new EvidenciaNoValidaExcepcion(
                "Se requiere al menos un comentario o una foto como evidencia"
        );
    }


    public static EvidenciaNoValidaExcepcion errorAlmacenamiento(String detalle) {
        return new EvidenciaNoValidaExcepcion(
                String.format("Error al almacenar la evidencia: %s", detalle)
        );
    }

    public static EvidenciaNoValidaExcepcion evidenciaNoEncontrada(Long evidenciaId) {
        return new EvidenciaNoValidaExcepcion(
                String.format("No se encontró la evidencia con ID: %d", evidenciaId)
        );
    }

    public static EvidenciaNoValidaExcepcion sinPermisosParaEliminar() {
        return new EvidenciaNoValidaExcepcion(
                "No tiene permisos para eliminar esta evidencia"
        );
    }
}

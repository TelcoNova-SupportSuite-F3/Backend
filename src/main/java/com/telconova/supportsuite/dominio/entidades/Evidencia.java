package com.telconova.supportsuite.dominio.entidades;

import com.telconova.supportsuite.dominio.enums.TipoEvidencia;
import com.telconova.supportsuite.dominio.excepciones.EvidenciaNoValidaExcepcion;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Entidad de dominio que representa una Evidencia de una orden de trabajo
 *
 * @author TelcoNova Development Team
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Evidencia {


    @EqualsAndHashCode.Include
    private Long id;

    private Long ordenTrabajoId;
    private TipoEvidencia tipo;
    private String contenido;
    private String rutaArchivo;
    private String nombreArchivoOriginal;
    private String tipoMime;
    private Long tamanoArchivo;
    private LocalDateTime fechaCreacion;
    private Long creadoPor;

    /**
     * Crea una evidencia de tipo comentario
     */
    public static Evidencia crearComentario(Long ordenTrabajoId, String comentario, Long usuarioId) {
        validarComentario(comentario);

        return Evidencia.builder()
                .ordenTrabajoId(ordenTrabajoId)
                .tipo(TipoEvidencia.COMENTARIO)
                .contenido(comentario.trim())
                .fechaCreacion(LocalDateTime.now())
                .creadoPor(usuarioId)
                .build();
    }

    /**
     * Crea una evidencia de tipo foto
     */
    public static Evidencia crearFoto(Long ordenTrabajoId, String rutaArchivo,
                                      String nombreOriginal, String tipoMime,
                                      Long tamanoArchivo, Long usuarioId) {
        validarFoto(rutaArchivo, nombreOriginal, tipoMime, tamanoArchivo);

        return Evidencia.builder()
                .ordenTrabajoId(ordenTrabajoId)
                .tipo(TipoEvidencia.FOTO)
                .rutaArchivo(rutaArchivo)
                .nombreArchivoOriginal(nombreOriginal)
                .tipoMime(tipoMime)
                .tamanoArchivo(tamanoArchivo)
                .fechaCreacion(LocalDateTime.now())
                .creadoPor(usuarioId)
                .build();
    }

    /**
     * Verifica si la evidencia es un comentario
     */
    public boolean esComentario() {
        return TipoEvidencia.COMENTARIO.equals(this.tipo);
    }

    /**
     * Verifica si la evidencia es una foto
     */
    public boolean esFoto() {
        return TipoEvidencia.FOTO.equals(this.tipo);
    }

    /**
     * Obtiene el tamaño del archivo en formato legible
     */
    public String getTamanoArchivoFormateado() {
        if (this.tamanoArchivo == null) {
            return "N/A";
        }

        double bytes = this.tamanoArchivo;
        String[] unidades = {"B", "KB", "MB", "GB"};
        int unidadIndex = 0;

        while (bytes >= 1024 && unidadIndex < unidades.length - 1) {
            bytes /= 1024;
            unidadIndex++;
        }

        return String.format("%.1f %s", bytes, unidades[unidadIndex]);
    }

    /**
     * Obtiene la extensión del archivo
     */
    public String getExtensionArchivo() {
        if (this.nombreArchivoOriginal == null) {
            return null;
        }

        int ultimoPunto = this.nombreArchivoOriginal.lastIndexOf('.');
        if (ultimoPunto == -1) {
            return null;
        }

        return this.nombreArchivoOriginal.substring(ultimoPunto + 1).toLowerCase();
    }

    private static void validarComentario(String comentario) {
        if (comentario == null || comentario.trim().isEmpty()) {
            throw EvidenciaNoValidaExcepcion.comentarioVacio();
        }

        if (comentario.trim().length() > TipoEvidencia.COMENTARIO.getTamaanoMaximo()) {
            throw EvidenciaNoValidaExcepcion.comentarioMuyLargo(
                    comentario.trim().length(),
                    (int) TipoEvidencia.COMENTARIO.getTamaanoMaximo()
            );
        }
    }

    private static void validarFoto(String rutaArchivo, String nombreOriginal,
                                    String tipoMime, Long tamanoArchivo) {
        if (rutaArchivo == null || rutaArchivo.trim().isEmpty()) {
            throw EvidenciaNoValidaExcepcion.archivoVacio();
        }

        if (nombreOriginal == null || nombreOriginal.trim().isEmpty()) {
            throw EvidenciaNoValidaExcepcion.archivoVacio();
        }

        if (tipoMime == null || !Arrays.asList(TipoEvidencia.tiposMimePermitidos()).contains(tipoMime)) {
            throw EvidenciaNoValidaExcepcion.tipoArchivoNoPermitido(tipoMime);
        }

        if (tamanoArchivo == null || tamanoArchivo <= 0) {
            throw new EvidenciaNoValidaExcepcion("El tamaño del archivo debe ser mayor a cero");
        }

        if (tamanoArchivo > TipoEvidencia.FOTO.getTamaanoMaximo()) {
            throw EvidenciaNoValidaExcepcion.archivoMuyGrande(
                    tamanoArchivo,
                    TipoEvidencia.FOTO.getTamaanoMaximo()
            );
        }
    }

    @Override
    public String toString() {
        return String.format("Evidencia{id=%d, tipo=%s, ordenId=%d, fecha=%s}",
                id, tipo, ordenTrabajoId, fechaCreacion);
    }
}

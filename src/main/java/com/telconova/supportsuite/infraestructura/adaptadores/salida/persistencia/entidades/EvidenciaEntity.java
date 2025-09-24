package com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.entidades;

import com.telconova.supportsuite.dominio.enums.TipoEvidencia;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad JPA para Evidencia
 */
@Entity
@Table(name = "evidencias", schema = "telconova")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvidenciaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "orden_trabajo_id", nullable = false)
    private Long ordenTrabajoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoEvidencia tipo;

    @Column(name = "contenido", columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "ruta_archivo")
    private String rutaArchivo;

    @Column(name = "nombre_archivo_original")
    private String nombreArchivoOriginal;

    @Column(name = "tipo_mime")
    private String tipoMime;

    @Column(name = "tamano_archivo")
    private Long tamanoArchivo;

    @CreationTimestamp
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "creado_por", nullable = false)
    private Long creadoPor;
}

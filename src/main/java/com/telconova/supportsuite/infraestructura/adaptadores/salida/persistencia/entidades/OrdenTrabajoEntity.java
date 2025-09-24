package com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.entidades;

import com.telconova.supportsuite.dominio.enums.EstadoOrden;
import com.telconova.supportsuite.dominio.enums.Prioridad;
import com.telconova.supportsuite.dominio.enums.TipoServicio;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidad JPA para Orden de Trabajo
 */
@Entity
@Table(name = "ordenes_trabajo", schema = "telconova")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdenTrabajoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_orden", nullable = false, unique = true)
    private String numeroOrden;

    @Column(name = "titulo", nullable = false)
    private String titulo;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoOrden estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad", nullable = false)
    private Prioridad prioridad;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_servicio", nullable = false)
    private TipoServicio tipoServicio;

    @Column(name = "cliente_nombre", nullable = false)
    private String clienteNombre;

    @Column(name = "cliente_telefono")
    private String clienteTelefono;

    @Column(name = "direccion", nullable = false, columnDefinition = "TEXT")
    private String direccion;

    @Column(name = "tecnico_asignado_id")
    private Long tecnicoAsignadoId;

    @Column(name = "fecha_asignacion")
    private LocalDateTime fechaAsignacion;

    @Column(name = "fecha_inicio_trabajo")
    private LocalDateTime fechaInicioTrabajo;

    @Column(name = "fecha_fin_trabajo")
    private LocalDateTime fechaFinTrabajo;

    @CreationTimestamp
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "ordenTrabajoId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EvidenciaEntity> evidencias;

    @OneToMany(mappedBy = "ordenTrabajoId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MaterialUtilizadoEntity> materialesUtilizados;
}

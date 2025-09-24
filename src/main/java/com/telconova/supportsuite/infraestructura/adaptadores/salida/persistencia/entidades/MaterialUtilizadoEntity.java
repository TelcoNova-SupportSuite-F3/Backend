package com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad JPA para Material Utilizado
 */
@Entity
@Table(name = "materiales_utilizados", schema = "telconova")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialUtilizadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "orden_trabajo_id", nullable = false)
    private Long ordenTrabajoId;

    @Column(name = "material_id", nullable = false)
    private Long materialId;

    @Column(name = "cantidad_utilizada", nullable = false)
    private Integer cantidadUtilizada;

    @Column(name = "precio_unitario", precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @CreationTimestamp
    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "registrado_por", nullable = false)
    private Long registradoPor;

    // Campos desnormalizados para auditoría
    @Column(name = "codigo_material")
    private String codigoMaterial;

    @Column(name = "nombre_material")
    private String nombreMaterial;

    @Column(name = "unidad_medida")
    private String unidadMedida;
}

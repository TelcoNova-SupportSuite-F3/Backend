package com.telconova.supportsuite.dominio.entidades;

import com.telconova.supportsuite.dominio.enums.EstadoOrden;
import com.telconova.supportsuite.dominio.enums.Prioridad;
import com.telconova.supportsuite.dominio.enums.TipoServicio;
import com.telconova.supportsuite.dominio.valueobjects.NumeroOrden;
import com.telconova.supportsuite.dominio.valueobjects.Telefono;
import com.telconova.supportsuite.dominio.excepciones.DominioExcepcion;
import com.telconova.supportsuite.dominio.excepciones.EstadoOrdenInvalidoExcepcion;
import lombok.*;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OrdenTrabajo {

    @EqualsAndHashCode.Include
    private Long id;

    private NumeroOrden numeroOrden;
    private String titulo;
    private String descripcion;
    private EstadoOrden estado;
    private Prioridad prioridad;
    private TipoServicio tipoServicio;
    private String clienteNombre;
    private Telefono clienteTelefono;
    private String direccion;
    private Long tecnicoAsignadoId;
    private LocalDateTime fechaAsignacion;
    private LocalDateTime fechaInicioTrabajo;
    private LocalDateTime fechaFinTrabajo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    // Relaciones (manejadas como agregados)
    @Builder.Default
    private List<Evidencia> evidencias = new ArrayList<>();

    @Builder.Default
    private List<MaterialUtilizado> materialesUtilizados = new ArrayList<>();

    /**
     * Constructor para crear una nueva orden de trabajo
     */
    public static OrdenTrabajo crear(NumeroOrden numeroOrden, String titulo, String descripcion,
                                     Prioridad prioridad, TipoServicio tipoServicio,
                                     String clienteNombre, Telefono clienteTelefono, String direccion) {
        validarDatosCreacion(numeroOrden, titulo, clienteNombre, direccion);

        return OrdenTrabajo.builder()
                .numeroOrden(numeroOrden)
                .titulo(titulo.trim())
                .descripcion(descripcion != null ? descripcion.trim() : null)
                .estado(EstadoOrden.ASIGNADA)
                .prioridad(prioridad != null ? prioridad : Prioridad.MEDIA)
                .tipoServicio(tipoServicio)
                .clienteNombre(clienteNombre.trim())
                .clienteTelefono(clienteTelefono)
                .direccion(direccion.trim())
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .evidencias(new ArrayList<>())
                .materialesUtilizados(new ArrayList<>())
                .build();
    }

    /**
     * Asigna la orden a un técnico
     */
    public void asignarTecnico(Long tecnicoId) {
        if (tecnicoId == null) {
            throw new DominioExcepcion("El ID del técnico no puede ser nulo");
        }

        if (this.estado != EstadoOrden.ASIGNADA && this.tecnicoAsignadoId != null) {
            throw new DominioExcepcion("La orden ya tiene un técnico asignado");
        }

        this.tecnicoAsignadoId = tecnicoId;
        this.fechaAsignacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Inicia el trabajo en la orden
     */
    public void iniciarTrabajo() {
        if (this.estado != EstadoOrden.ASIGNADA) {
            throw EstadoOrdenInvalidoExcepcion.transicionInvalida(this.estado, EstadoOrden.EN_PROCESO);
        }

        if (this.tecnicoAsignadoId == null) {
            throw new DominioExcepcion("No se puede iniciar el trabajo sin tener un técnico asignado");
        }

        this.estado = EstadoOrden.EN_PROCESO;
        this.fechaInicioTrabajo = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Pausa la orden
     */
    public void pausar() {
        if (!this.estado.puedeTransicionarA(EstadoOrden.PAUSADA)) {
            throw EstadoOrdenInvalidoExcepcion.transicionInvalida(this.estado, EstadoOrden.PAUSADA);
        }

        this.estado = EstadoOrden.PAUSADA;
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Reanuda la orden desde pausa
     */
    public void reanudar() {
        if (this.estado != EstadoOrden.PAUSADA) {
            throw EstadoOrdenInvalidoExcepcion.transicionInvalida(this.estado, EstadoOrden.EN_PROCESO);
        }

        this.estado = EstadoOrden.EN_PROCESO;
        this.fechaActualizacion = LocalDateTime.now();
    }

    public void finalizar(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        // Validar que se puede finalizar desde el estado actual
        if (!EstadoOrden.estadosParaFinalizar().contains(this.estado)) {
            throw EstadoOrdenInvalidoExcepcion.paraFinalizar(this.estado);
        }

        // Validar fechas obligatorias
        if (fechaInicio == null || fechaFin == null) {
            throw new DominioExcepcion("Las fechas de inicio y fin son obligatorias para finalizar una orden");
        }

        if (fechaFin.isBefore(fechaInicio)) {
            throw new DominioExcepcion("La fecha de fin no puede ser anterior a la fecha de inicio");
        }

        // Validar que hay al menos una evidencia
        if (!tieneEvidencias()) {
            throw new DominioExcepcion("Se requiere al menos un comentario o foto para finalizar la orden");
        }

        this.estado = EstadoOrden.FINALIZADA;
        this.fechaInicioTrabajo = fechaInicio;
        this.fechaFinTrabajo = fechaFin;
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Agrega una evidencia a la orden
     */
    public void agregarEvidencia(Evidencia evidencia) {
        if (evidencia == null) {
            throw new DominioExcepcion("La evidencia no puede ser nula");
        }

        this.evidencias.add(evidencia);
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Agrega un material utilizado a la orden
     */
    public void agregarMaterialUtilizado(MaterialUtilizado materialUtilizado) {
        if (materialUtilizado == null) {
            throw new DominioExcepcion("El material utilizado no puede ser nulo");
        }

        // Solo se pueden agregar materiales si la orden está EN_PROCESO
        if (!EstadoOrden.estadosParaAgregarMateriales().contains(this.estado)) {
            throw EstadoOrdenInvalidoExcepcion.paraAgregarMateriales(this.estado);
        }

        // Verificar si ya existe el material y actualizar cantidad
        MaterialUtilizado existente = this.materialesUtilizados.stream()
                .filter(m -> m.getMaterialId().equals(materialUtilizado.getMaterialId()))
                .findFirst()
                .orElse(null);

        if (existente != null) {
            existente.incrementarCantidad(materialUtilizado.getCantidadUtilizada());
        } else {
            this.materialesUtilizados.add(materialUtilizado);
        }

        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Actualiza la prioridad de la orden
     */
    public void actualizarPrioridad(Prioridad nuevaPrioridad) {
        if (nuevaPrioridad == null) {
            throw new DominioExcepcion("La prioridad no puede ser nula");
        }

        if (this.estado == EstadoOrden.FINALIZADA) {
            throw new DominioExcepcion("No se puede cambiar la prioridad de una orden finalizada");
        }

        this.prioridad = nuevaPrioridad;
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Verifica si la orden tiene evidencias
     */
    public boolean tieneEvidencias() {
        return !this.evidencias.isEmpty();
    }

    /**
     * Verifica si la orden tiene materiales utilizados
     */
    public boolean tieneMaterialesUtilizados() {
        return !this.materialesUtilizados.isEmpty();
    }

    /**
     * Verifica si la orden está asignada a un técnico específico
     */
    public boolean estaAsignadaATecnico(Long tecnicoId) {
        return this.tecnicoAsignadoId != null && this.tecnicoAsignadoId.equals(tecnicoId);
    }

    /**
     * Verifica si la orden está en un estado activo
     */
    public boolean estaActiva() {
        return EstadoOrden.estadosActivos().contains(this.estado);
    }

    /**
     * Verifica si la orden está finalizada
     */
    public boolean estaFinalizada() {
        return EstadoOrden.FINALIZADA.equals(this.estado);
    }

    /**
     * Verifica si se pueden agregar materiales
     */
    public boolean puedeAgregarMateriales() {
        return EstadoOrden.estadosParaAgregarMateriales().contains(this.estado);
    }

    /**
     * Calcula la duración del trabajo si está finalizada
     */
    public Duration getDuracionTrabajo() {
        if (this.fechaInicioTrabajo == null || this.fechaFinTrabajo == null) {
            return Duration.ZERO;
        }
        return Duration.between(this.fechaInicioTrabajo, this.fechaFinTrabajo);
    }

    /**
     * Obtiene las evidencias de forma inmutable
     */
    public List<Evidencia> getEvidencias() {
        return Collections.unmodifiableList(this.evidencias);
    }

    /**
     * Obtiene los materiales utilizados de forma inmutable
     */
    public List<MaterialUtilizado> getMaterialesUtilizados() {
        return Collections.unmodifiableList(this.materialesUtilizados);
    }

    /**
     * Calcula el costo total de materiales utilizados
     */
    public double getCostoTotalMateriales() {
        return this.materialesUtilizados.stream()
                .mapToDouble(MaterialUtilizado::getCostoTotal)
                .sum();
    }

    /**
     * Obtiene el resumen de la orden
     */
    public String getResumen() {
        return String.format("Orden %s - %s (%s) - Cliente: %s - Estado: %s",
                this.numeroOrden.getValor(),
                this.titulo,
                this.prioridad.getDescripcion(),
                this.clienteNombre,
                this.estado.getDescripcion()
        );
    }

    /**
     * Verifica si la orden es urgente
     */
    public boolean esUrgente() {
        return this.prioridad.esUrgente();
    }

    /**
     * Obtiene los días transcurridos desde la creación
     */
    public long getDiasTranscurridos() {
        return Duration.between(this.fechaCreacion, LocalDateTime.now()).toDays();
    }

    /**
     * Verifica si la orden está vencida (más de 7 días sin finalizar)
     */
    public boolean estaVencida() {
        return !estaFinalizada() && getDiasTranscurridos() > 7;
    }

    private static void validarDatosCreacion(NumeroOrden numeroOrden, String titulo,
                                             String clienteNombre, String direccion) {
        if (numeroOrden == null) {
            throw new DominioExcepcion("El número de orden es obligatorio");
        }

        if (titulo == null || titulo.trim().isEmpty()) {
            throw new DominioExcepcion("El título es obligatorio");
        }

        if (titulo.trim().length() > 255) {
            throw new DominioExcepcion("El título no puede exceder 255 caracteres");
        }

        if (clienteNombre == null || clienteNombre.trim().isEmpty()) {
            throw new DominioExcepcion("El nombre del cliente es obligatorio");
        }

        if (clienteNombre.trim().length() > 255) {
            throw new DominioExcepcion("El nombre del cliente no puede exceder 255 caracteres");
        }

        if (direccion == null || direccion.trim().isEmpty()) {
            throw new DominioExcepcion("La dirección es obligatoria");
        }

        if (direccion.trim().length() > 500) {
            throw new DominioExcepcion("La dirección no puede exceder 500 caracteres");
        }
    }

    @Override
    public String toString() {
        return String.format("OrdenTrabajo{id=%d, numeroOrden='%s', titulo='%s', estado=%s, prioridad=%s, cliente='%s'}",
                id,
                numeroOrden != null ? numeroOrden.getValor() : "null",
                titulo,
                estado,
                prioridad,
                clienteNombre
        );
    }
}

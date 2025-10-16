package com.telconova.supportsuite.dominio.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Tests para Enums del dominio")
class EnumsTest {

    // ================== TESTS PARA PRIORIDAD ==================

    @Test
    @DisplayName("Debe verificar niveles de prioridad correctamente")
    void debeVerificarNivelesPrioridad() {
        // Arrange & Act & Assert
        assertThat(Prioridad.BAJA.getNivel()).isEqualTo(1);
        assertThat(Prioridad.MEDIA.getNivel()).isEqualTo(2);
        assertThat(Prioridad.ALTA.getNivel()).isEqualTo(3);
        assertThat(Prioridad.CRITICA.getNivel()).isEqualTo(4);
    }

    @Test
    @DisplayName("Debe verificar descripciones de prioridad")
    void debeVerificarDescripcionesPrioridad() {
        // Arrange & Act & Assert
        assertThat(Prioridad.BAJA.getDescripcion()).isEqualTo("Baja");
        assertThat(Prioridad.MEDIA.getDescripcion()).isEqualTo("Media");
        assertThat(Prioridad.ALTA.getDescripcion()).isEqualTo("Alta");
        assertThat(Prioridad.CRITICA.getDescripcion()).isEqualTo("Crítica");
    }

    @Test
    @DisplayName("Debe verificar detalles de prioridad")
    void debeVerificarDetallesPrioridad() {
        // Arrange & Act & Assert
        assertThat(Prioridad.BAJA.getDetalle()).isEqualTo("Prioridad baja - No urgente");
        assertThat(Prioridad.MEDIA.getDetalle()).isEqualTo("Prioridad media - Atención normal");
        assertThat(Prioridad.ALTA.getDetalle()).isEqualTo("Prioridad alta - Requiere atención pronta");
        assertThat(Prioridad.CRITICA.getDetalle()).isEqualTo("Prioridad crítica - Atención inmediata");
    }

    @Test
    @DisplayName("Debe comparar prioridades correctamente")
    void debeCompararPrioridadesCorrectamente() {
        // Arrange & Act & Assert
        assertThat(Prioridad.CRITICA.esMayorQue(Prioridad.ALTA)).isTrue();
        assertThat(Prioridad.ALTA.esMayorQue(Prioridad.MEDIA)).isTrue();
        assertThat(Prioridad.MEDIA.esMayorQue(Prioridad.BAJA)).isTrue();
        assertThat(Prioridad.BAJA.esMayorQue(Prioridad.CRITICA)).isFalse();
        assertThat(Prioridad.MEDIA.esMayorQue(Prioridad.ALTA)).isFalse();
    }

    @Test
    @DisplayName("Debe identificar prioridades urgentes")
    void debeIdentificarPrioridadesUrgentes() {
        // Arrange & Act & Assert
        assertThat(Prioridad.CRITICA.esUrgente()).isTrue();
        assertThat(Prioridad.ALTA.esUrgente()).isTrue();
        assertThat(Prioridad.MEDIA.esUrgente()).isFalse();
        assertThat(Prioridad.BAJA.esUrgente()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(Prioridad.class)
    @DisplayName("Debe tener valores no nulos para cada prioridad")
    void debeTenerValoresNoNulosParaPrioridad(Prioridad prioridad) {
        // Arrange & Act & Assert
        assertThat(prioridad.getNivel()).isPositive();
        assertThat(prioridad.getDescripcion()).isNotNull().isNotEmpty();
        assertThat(prioridad.getDetalle()).isNotNull().isNotEmpty();
    }

    // ================== TESTS PARA ROL USUARIO ==================

    @Test
    @DisplayName("Debe verificar descripciones de roles")
    void debeVerificarDescripcionesRoles() {
        // Arrange & Act & Assert
        assertThat(RolUsuario.TECNICO.getDescripcion()).isEqualTo("Técnico");
        assertThat(RolUsuario.ADMIN.getDescripcion()).isEqualTo("Administrador");
    }

    @ParameterizedTest
    @EnumSource(RolUsuario.class)
    @DisplayName("Debe tener descripción no nula para cada rol")
    void debeTenerDescripcionNoNulaParaRol(RolUsuario rol) {
        // Arrange & Act & Assert
        assertThat(rol.getDescripcion()).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("Debe verificar todos los roles disponibles")
    void debeVerificarTodosLosRolesDisponibles() {
        // Arrange & Act
        RolUsuario[] roles = RolUsuario.values();

        // Assert
        assertThat(roles)
                .hasSize(2)
                .contains(RolUsuario.TECNICO, RolUsuario.ADMIN);
    }

    // ================== TESTS PARA TIPO EVIDENCIA ==================

    @Test
    @DisplayName("Debe verificar descripciones de tipo evidencia")
    void debeVerificarDescripcionesTipoEvidencia() {
        // Arrange & Act & Assert
        assertThat(TipoEvidencia.COMENTARIO.getDescripcion()).isEqualTo("Comentario");
        assertThat(TipoEvidencia.FOTO.getDescripcion()).isEqualTo("Foto");
    }

    @Test
    @DisplayName("Debe verificar detalles de tipo evidencia")
    void debeVerificarDetallesTipoEvidencia() {
        // Arrange & Act & Assert
        assertThat(TipoEvidencia.COMENTARIO.getDetalle()).isEqualTo("Comentario de texto del técnico");
        assertThat(TipoEvidencia.FOTO.getDetalle()).isEqualTo("Fotografía como evidencia visual");
    }

    @Test
    @DisplayName("Debe verificar tamaños máximos de evidencia")
    void debeVerificarTamanosMaximosEvidencia() {
        // Arrange & Act & Assert
        assertThat(TipoEvidencia.COMENTARIO.getTamaanoMaximo()).isEqualTo(500L);
        assertThat(TipoEvidencia.FOTO.getTamaanoMaximo()).isEqualTo(10485760L); // 10MB
    }

    @Test
    @DisplayName("Debe identificar tipo foto correctamente")
    void debeIdentificarTipoFotoCorrectamente() {
        // Arrange & Act & Assert
        assertThat(TipoEvidencia.FOTO.esFoto()).isTrue();
        assertThat(TipoEvidencia.COMENTARIO.esFoto()).isFalse();
    }

    @Test
    @DisplayName("Debe identificar tipo comentario correctamente")
    void debeIdentificarTipoComentarioCorrectamente() {
        // Arrange & Act & Assert
        assertThat(TipoEvidencia.COMENTARIO.esComentario()).isTrue();
        assertThat(TipoEvidencia.FOTO.esComentario()).isFalse();
    }

    @Test
    @DisplayName("Debe obtener tipos MIME permitidos")
    void debeObtenerTiposMimePermitidos() {
        // Arrange & Act
        String[] tiposMime = TipoEvidencia.tiposMimePermitidos();

        // Assert
        assertThat(tiposMime)
                .isNotNull()
                .hasSize(4)
                .contains("image/jpeg", "image/jpg", "image/png", "image/svg");
    }

    // ================== TESTS PARA TIPO SERVICIO ==================

    @Test
    @DisplayName("Debe verificar descripciones de tipo servicio")
    void debeVerificarDescripcionesTipoServicio() {
        // Arrange & Act & Assert
        assertThat(TipoServicio.INSTALACION.getDescripcion()).isEqualTo("Instalación");
        assertThat(TipoServicio.REPARACION.getDescripcion()).isEqualTo("Reparación");
        assertThat(TipoServicio.MANTENIMIENTO.getDescripcion()).isEqualTo("Mantenimiento");
        assertThat(TipoServicio.UPGRADE.getDescripcion()).isEqualTo("Upgrade");
        assertThat(TipoServicio.DESCONEXION.getDescripcion()).isEqualTo("Desconexión");
        assertThat(TipoServicio.RECONEXION.getDescripcion()).isEqualTo("Reconexión");
        assertThat(TipoServicio.REVISION_TECNICA.getDescripcion()).isEqualTo("Revisión Técnica");
    }

    @Test
    @DisplayName("Debe verificar detalles de tipo servicio")
    void debeVerificarDetallesTipoServicio() {
        // Arrange & Act & Assert
        assertThat(TipoServicio.INSTALACION.getDetalle()).isEqualTo("Instalación de nuevos servicios");
        assertThat(TipoServicio.REPARACION.getDetalle()).isEqualTo("Reparación de servicios existentes");
        assertThat(TipoServicio.MANTENIMIENTO.getDetalle()).isEqualTo("Mantenimiento preventivo o correctivo");
        assertThat(TipoServicio.UPGRADE.getDetalle()).isEqualTo("Actualización o mejora de servicios");
        assertThat(TipoServicio.DESCONEXION.getDetalle()).isEqualTo("Desconexión de servicios");
        assertThat(TipoServicio.RECONEXION.getDetalle()).isEqualTo("Reconexión de servicios suspendidos");
        assertThat(TipoServicio.REVISION_TECNICA.getDetalle()).isEqualTo("Revisión técnica especializada");
    }

    @Test
    @DisplayName("Debe identificar servicios que requieren materiales")
    void debeIdentificarServiciosQueRequierenMateriales() {
        // Arrange & Act & Assert
        assertThat(TipoServicio.INSTALACION.requiereMaterialesGeneralmente()).isTrue();
        assertThat(TipoServicio.REPARACION.requiereMaterialesGeneralmente()).isTrue();
        assertThat(TipoServicio.UPGRADE.requiereMaterialesGeneralmente()).isTrue();

        assertThat(TipoServicio.MANTENIMIENTO.requiereMaterialesGeneralmente()).isFalse();
        assertThat(TipoServicio.DESCONEXION.requiereMaterialesGeneralmente()).isFalse();
        assertThat(TipoServicio.RECONEXION.requiereMaterialesGeneralmente()).isFalse();
        assertThat(TipoServicio.REVISION_TECNICA.requiereMaterialesGeneralmente()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(TipoServicio.class)
    @DisplayName("Debe tener valores no nulos para cada tipo servicio")
    void debeTenerValoresNoNulosParaTipoServicio(TipoServicio tipoServicio) {
        // Arrange & Act & Assert
        assertThat(tipoServicio.getDescripcion()).isNotNull().isNotEmpty();
        assertThat(tipoServicio.getDetalle()).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("Debe verificar todos los tipos de servicio disponibles")
    void debeVerificarTodosLosTiposServicioDisponibles() {
        // Arrange & Act
        TipoServicio[] tipos = TipoServicio.values();

        // Assert
        assertThat(tipos)
                .hasSize(7)
                .contains(
                TipoServicio.INSTALACION,
                TipoServicio.REPARACION,
                TipoServicio.MANTENIMIENTO,
                TipoServicio.UPGRADE,
                TipoServicio.DESCONEXION,
                TipoServicio.RECONEXION,
                TipoServicio.REVISION_TECNICA
        );
    }

    // ================== TESTS ADICIONALES DE COBERTURA ==================

    @Test
    @DisplayName("Debe obtener valores de enum por nombre")
    void debeObtenerValoresEnumPorNombre() {
        // Arrange & Act & Assert
        assertThat(Prioridad.valueOf("ALTA")).isEqualTo(Prioridad.ALTA);
        assertThat(RolUsuario.valueOf("TECNICO")).isEqualTo(RolUsuario.TECNICO);
        assertThat(TipoEvidencia.valueOf("FOTO")).isEqualTo(TipoEvidencia.FOTO);
        assertThat(TipoServicio.valueOf("INSTALACION")).isEqualTo(TipoServicio.INSTALACION);
    }

    @Test
    @DisplayName("Debe lanzar excepción para valores de enum inválidos")
    void debeLanzarExcepcionParaValoresInvalidos() {
        // Arrange & Act & Assert
        assertThatThrownBy(() -> Prioridad.valueOf("INEXISTENTE"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> RolUsuario.valueOf("SUPERADMIN"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> TipoEvidencia.valueOf("VIDEO"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> TipoServicio.valueOf("INVENTARIO"))
                .isInstanceOf(IllegalArgumentException.class);
    }

}

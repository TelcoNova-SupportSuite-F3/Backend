package com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.mappers;

import com.telconova.supportsuite.dominio.entidades.*;
import com.telconova.supportsuite.dominio.enums.*;
import com.telconova.supportsuite.dominio.valueobjects.*;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.entidades.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para clase UsuarioMapper")
class UsuarioMapperTest {

    private EvidenciaMapper evidenciaMapper;
    private UsuarioMapper usuarioMapper;

    @BeforeEach
    void setUp() {
        evidenciaMapper = new EvidenciaMapper();
        usuarioMapper = new UsuarioMapper();
    }

    @Test
    @DisplayName("Debe convertir UsuarioEntity a Usuario correctamente")
    void debeConvertirUsuarioEntityADominio() {
        // Arrange
        UsuarioEntity entity = UsuarioEntity.builder()
                .id(1L)
                .email("tecnico@telconova.com")
                .contrasena("$2a$10$hashedpassword")
                .nombreCompleto("Juan Pérez")
                .rol(RolUsuario.TECNICO)
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        // Act
        Usuario dominio = usuarioMapper.toDomain(entity);

        // Assert
        assertThat(dominio).isNotNull();
        assertThat(dominio.getId()).isEqualTo(1L);
        assertThat(dominio.getEmail().getValor()).isEqualTo("tecnico@telconova.com");
        assertThat(dominio.getContrasenaEncriptada()).isEqualTo("$2a$10$hashedpassword");
        assertThat(dominio.getNombreCompleto()).isEqualTo("Juan Pérez");
        assertThat(dominio.getRol()).isEqualTo(RolUsuario.TECNICO);
        assertThat(dominio.estaActivo()).isTrue();
    }

    @Test
    @DisplayName("Debe convertir Usuario a UsuarioEntity correctamente")
    void debeConvertirUsuarioAEntity() {
        // Arrange
        Usuario dominio = Usuario.builder()
                .id(2L)
                .email(Email.de("admin@telconova.com"))
                .contrasenaEncriptada("$2a$10$anotherhashedpassword")
                .nombreCompleto("Admin Sistema")
                .rol(RolUsuario.ADMIN)
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        // Act
        UsuarioEntity entity = usuarioMapper.toEntity(dominio);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(2L);
        assertThat(entity.getEmail()).isEqualTo("admin@telconova.com");
        assertThat(entity.getContrasena()).isEqualTo("$2a$10$anotherhashedpassword");
        assertThat(entity.getNombreCompleto()).isEqualTo("Admin Sistema");
        assertThat(entity.getRol()).isEqualTo(RolUsuario.ADMIN);
        assertThat(entity.getActivo()).isTrue();
    }

    @Test
    @DisplayName("Debe manejar null en UsuarioMapper")
    void debeManejarNullEnUsuarioMapper() {
        // Act & Assert
        assertThat(usuarioMapper.toDomain(null)).isNull();
        assertThat(usuarioMapper.toEntity(null)).isNull();
    }

    @Test
    @DisplayName("Debe manejar usuario inactivo correctamente")
    void debeManejarUsuarioInactivoCorrectamente() {
        // Arrange
        UsuarioEntity entity = UsuarioEntity.builder()
                .id(3L)
                .email("inactivo@telconova.com")
                .contrasena("password")
                .nombreCompleto("Usuario Inactivo")
                .rol(RolUsuario.TECNICO)
                .activo(false)
                .fechaCreacion(LocalDateTime.now())
                .build();

        // Act
        Usuario dominio = usuarioMapper.toDomain(entity);

        // Assert
        assertThat(dominio).isNotNull();
        assertThat(dominio.estaActivo()).isFalse();
    }


    @Test
    @DisplayName("Debe mapear todos los campos de evidencia con foto")
    void debeMapearTodosCamposEvidenciaConFoto() {
        // Arrange
        LocalDateTime fecha = LocalDateTime.now();
        EvidenciaEntity entity = EvidenciaEntity.builder()
                .id(100L)
                .ordenTrabajoId(200L)
                .tipo(TipoEvidencia.FOTO)
                .contenido(null)
                .rutaArchivo("cloudinary/path/foto.jpg")
                .nombreArchivoOriginal("evidencia_original.jpg")
                .tipoMime("image/jpeg")
                .tamanoArchivo(2048576L)
                .fechaCreacion(fecha)
                .creadoPor(1L)
                .build();

        // Act
        Evidencia dominio = evidenciaMapper.toDomain(entity);
        EvidenciaEntity entityResult = evidenciaMapper.toEntity(dominio);

        // Assert
        assertThat(dominio).isNotNull();
        assertThat(dominio.getTamanoArchivo()).isEqualTo(2048576L);
        assertThat(dominio.getTipoMime()).isEqualTo("image/jpeg");

        assertThat(entityResult).isNotNull();
        assertThat(entityResult.getTamanoArchivo()).isEqualTo(2048576L);
        assertThat(entityResult.getTipoMime()).isEqualTo("image/jpeg");
    }


    @Test
    @DisplayName("Debe convertir Evidencia a EvidenciaEntity correctamente")
    void debeConvertirEvidenciaAEntity() {
        // Arrange
        Evidencia dominio = Evidencia.builder()
                .id(1L)
                .ordenTrabajoId(100L)
                .tipo(TipoEvidencia.COMENTARIO)
                .contenido("Comentario de prueba")
                .rutaArchivo(null)
                .nombreArchivoOriginal(null)
                .tipoMime(null)
                .tamanoArchivo(null)
                .fechaCreacion(LocalDateTime.now())
                .creadoPor(1L)
                .build();

        // Act
        EvidenciaEntity entity = evidenciaMapper.toEntity(dominio);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getOrdenTrabajoId()).isEqualTo(100L);
        assertThat(entity.getTipo()).isEqualTo(TipoEvidencia.COMENTARIO);
        assertThat(entity.getContenido()).isEqualTo("Comentario de prueba");
        assertThat(entity.getRutaArchivo()).isNull();
        assertThat(entity.getCreadoPor()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Debe manejar null en EvidenciaMapper")
    void debeManejarNullEnEvidenciaMapper() {
        // Act & Assert
        assertThat(evidenciaMapper.toDomain(null)).isNull();
        assertThat(evidenciaMapper.toEntity(null)).isNull();
    }

}

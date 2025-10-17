package com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia;

import com.telconova.supportsuite.dominio.entidades.Usuario;
import com.telconova.supportsuite.dominio.enums.RolUsuario;
import com.telconova.supportsuite.dominio.valueobjects.Email;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.entidades.UsuarioEntity;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.mappers.UsuarioMapper;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.repositorios.UsuarioJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests for UsuarioRepositoryImpl")
class UsuarioRepositoryImplTest {

    @Mock
    private UsuarioJpaRepository jpaRepository;

    @Mock
    private UsuarioMapper mapper;

    @InjectMocks
    private UsuarioRepositoryImpl usuarioRepository;

    private Usuario usuarioDominio;
    private UsuarioEntity usuarioEntity;
    private Email emailUsuario;

    @BeforeEach
    void setUp() {
        // Configurar email como value object
        emailUsuario = Email.de("juan.perez@telconova.com");

        // Configurar usuario de dominio
        usuarioDominio = Usuario.builder()
                .id(1L)
                .email(emailUsuario)
                .contrasenaEncriptada("$2a$10$hashedPassword")
                .nombreCompleto("Juan Pérez")
                .rol(RolUsuario.TECNICO)
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now().plusHours(1))
                .build();

        // Configurar usuario entity
        usuarioEntity = new UsuarioEntity();
        usuarioEntity.setId(1L);
        usuarioEntity.setEmail("juan.perez@telconova.com");
        usuarioEntity.setContrasena("$2a$10$hashedPassword");
        usuarioEntity.setNombreCompleto("Juan Pérez");
        usuarioEntity.setRol(RolUsuario.TECNICO);
        usuarioEntity.setActivo(true);
        usuarioEntity.setFechaCreacion(LocalDateTime.now());
        usuarioEntity.setFechaActualizacion(LocalDateTime.now().plusHours(1));
    }

    @Test
    void testBuscarPorEmail_Encontrado() {
        // Arrange
        String email = "juan.perez@telconova.com";
        when(jpaRepository.findByEmail(email)).thenReturn(Optional.of(usuarioEntity));
        when(mapper.toDomain(usuarioEntity)).thenReturn(usuarioDominio);

        // Act
        Optional<Usuario> resultado = usuarioRepository.buscarPorEmail(email);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(usuarioDominio.getId(), resultado.get().getId());
        assertEquals(email, resultado.get().getEmail().getValor());
        assertEquals(usuarioDominio.getNombreCompleto(), resultado.get().getNombreCompleto());
        assertEquals(usuarioDominio.getRol(), resultado.get().getRol());

        verify(jpaRepository).findByEmail(email);
        verify(mapper).toDomain(usuarioEntity);
    }

    @Test
    void testBuscarPorEmail_NoEncontrado() {
        // Arrange
        String email = "noexiste@telconova.com";
        when(jpaRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        Optional<Usuario> resultado = usuarioRepository.buscarPorEmail(email);

        // Assert
        assertFalse(resultado.isPresent());
        verify(jpaRepository).findByEmail(email);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    void testBuscarPorEmail_EmailEnMinusculas() {
        // Arrange
        String email = "juan.perez@telconova.com";
        when(jpaRepository.findByEmail(email)).thenReturn(Optional.of(usuarioEntity));
        when(mapper.toDomain(usuarioEntity)).thenReturn(usuarioDominio);

        // Act
        Optional<Usuario> resultado = usuarioRepository.buscarPorEmail(email);

        // Assert
        assertTrue(resultado.isPresent());
        verify(jpaRepository).findByEmail(email);
    }

    @Test
    void testBuscarPorId_Encontrado() {
        // Arrange
        Long usuarioId = 1L;
        when(jpaRepository.findById(usuarioId)).thenReturn(Optional.of(usuarioEntity));
        when(mapper.toDomain(usuarioEntity)).thenReturn(usuarioDominio);

        // Act
        Optional<Usuario> resultado = usuarioRepository.buscarPorId(usuarioId);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(usuarioId, resultado.get().getId());
        assertEquals(usuarioDominio.getEmail().getValor(), resultado.get().getEmail().getValor());
        assertEquals(usuarioDominio.getNombreCompleto(), resultado.get().getNombreCompleto());

        verify(jpaRepository).findById(usuarioId);
        verify(mapper).toDomain(usuarioEntity);
    }

    @Test
    void testBuscarPorId_NoEncontrado() {
        // Arrange
        Long usuarioId = 999L;
        when(jpaRepository.findById(usuarioId)).thenReturn(Optional.empty());

        // Act
        Optional<Usuario> resultado = usuarioRepository.buscarPorId(usuarioId);

        // Assert
        assertFalse(resultado.isPresent());
        verify(jpaRepository).findById(usuarioId);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    void testGuardar_UsuarioNuevo() {
        // Arrange
        Email nuevoEmail = Email.de("nuevo.usuario@telconova.com");
        Usuario usuarioNuevo = Usuario.builder()
                .email(nuevoEmail)
                .nombreCompleto("Nuevo Usuario")
                .contrasenaEncriptada("$2a$10$newHashedPassword")
                .rol(RolUsuario.ADMIN)
                .activo(true)
                .build();

        UsuarioEntity entityNueva = new UsuarioEntity();
        entityNueva.setEmail("nuevo.usuario@telconova.com");
        entityNueva.setNombreCompleto("Nuevo Usuario");

        UsuarioEntity entityGuardada = new UsuarioEntity();
        entityGuardada.setId(2L);
        entityGuardada.setEmail("nuevo.usuario@telconova.com");
        entityGuardada.setNombreCompleto("Nuevo Usuario");

        Usuario usuarioGuardado = Usuario.builder()
                .id(2L)
                .email(nuevoEmail)
                .nombreCompleto("Nuevo Usuario")
                .rol(RolUsuario.ADMIN)
                .activo(true)
                .build();

        when(mapper.toEntity(usuarioNuevo)).thenReturn(entityNueva);
        when(jpaRepository.save(entityNueva)).thenReturn(entityGuardada);
        when(mapper.toDomain(entityGuardada)).thenReturn(usuarioGuardado);

        // Act
        Usuario resultado = usuarioRepository.guardar(usuarioNuevo);

        // Assert
        assertNotNull(resultado);
        assertNotNull(resultado.getId());
        assertEquals(2L, resultado.getId());
        assertEquals("nuevo.usuario@telconova.com", resultado.getEmail().getValor());

        verify(mapper).toEntity(usuarioNuevo);
        verify(jpaRepository).save(entityNueva);
        verify(mapper).toDomain(entityGuardada);
    }

    @Test
    void testGuardar_ActualizarUsuarioExistente() {
        // Arrange
        Usuario usuarioActualizado = Usuario.builder()
                .id(1L)
                .email(emailUsuario)
                .nombreCompleto("Juan Pérez Actualizado")
                .contrasenaEncriptada("$2a$10$hashedPassword")
                .rol(RolUsuario.TECNICO)
                .activo(true)
                .build();

        UsuarioEntity entityActualizada = new UsuarioEntity();
        entityActualizada.setId(1L);
        entityActualizada.setNombreCompleto("Juan Pérez Actualizado");

        when(mapper.toEntity(usuarioActualizado)).thenReturn(entityActualizada);
        when(jpaRepository.save(entityActualizada)).thenReturn(entityActualizada);
        when(mapper.toDomain(entityActualizada)).thenReturn(usuarioActualizado);

        // Act
        Usuario resultado = usuarioRepository.guardar(usuarioActualizado);

        // Assert
        assertNotNull(resultado);
        assertEquals("Juan Pérez Actualizado", resultado.getNombreCompleto());
        verify(jpaRepository).save(entityActualizada);
    }

    @Test
    void testGuardar_UsuarioConTodosLosCampos() {
        // Arrange
        when(mapper.toEntity(usuarioDominio)).thenReturn(usuarioEntity);
        when(jpaRepository.save(usuarioEntity)).thenReturn(usuarioEntity);
        when(mapper.toDomain(usuarioEntity)).thenReturn(usuarioDominio);

        // Act
        Usuario resultado = usuarioRepository.guardar(usuarioDominio);

        // Assert
        assertNotNull(resultado);
        assertEquals(usuarioDominio.getId(), resultado.getId());
        assertEquals(usuarioDominio.getEmail().getValor(), resultado.getEmail().getValor());
        assertEquals(usuarioDominio.getNombreCompleto(), resultado.getNombreCompleto());
        assertEquals(usuarioDominio.getRol(), resultado.getRol());
        assertTrue(resultado.isActivo());

        verify(mapper).toEntity(usuarioDominio);
        verify(jpaRepository).save(usuarioEntity);
        verify(mapper).toDomain(usuarioEntity);
    }

    @Test
    void testExistePorEmail_Existe() {
        // Arrange
        String email = "juan.perez@telconova.com";
        when(jpaRepository.existsByEmail(email)).thenReturn(true);

        // Act
        boolean resultado = usuarioRepository.existePorEmail(email);

        // Assert
        assertTrue(resultado);
        verify(jpaRepository).existsByEmail(email);
    }

    @Test
    void testExistePorEmail_NoExiste() {
        // Arrange
        String email = "noexiste@telconova.com";
        when(jpaRepository.existsByEmail(email)).thenReturn(false);

        // Act
        boolean resultado = usuarioRepository.existePorEmail(email);

        // Assert
        assertFalse(resultado);
        verify(jpaRepository).existsByEmail(email);
    }

    @Test
    void testObtenerUsuariosActivos_ConResultados() {
        // Arrange
        UsuarioEntity entity2 = new UsuarioEntity();
        entity2.setId(2L);
        entity2.setEmail("maria.garcia@telconova.com");
        entity2.setNombreCompleto("María García");
        entity2.setRol(RolUsuario.ADMIN);
        entity2.setActivo(true);
        Email email2 = Email.de("maria.garcia@telconova.com");

        Usuario usuario2 = Usuario.builder()
                .id(2L)
                .email(email2)
                .nombreCompleto("María García")
                .rol(RolUsuario.ADMIN)
                .activo(true)
                .build();

        List<UsuarioEntity> entities = Arrays.asList(usuarioEntity, entity2);

        when(jpaRepository.findByActivoTrue()).thenReturn(entities);
        when(mapper.toDomain(usuarioEntity)).thenReturn(usuarioDominio);
        when(mapper.toDomain(entity2)).thenReturn(usuario2);

        // Act
        List<Usuario> resultado = usuarioRepository.obtenerUsuariosActivos();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(Usuario::isActivo));

        verify(jpaRepository).findByActivoTrue();
        verify(mapper, times(2)).toDomain(any(UsuarioEntity.class));
    }

    @Test
    void testObtenerUsuariosActivos_SinResultados() {
        // Arrange
        when(jpaRepository.findByActivoTrue()).thenReturn(Collections.emptyList());

        // Act
        List<Usuario> resultado = usuarioRepository.obtenerUsuariosActivos();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(jpaRepository).findByActivoTrue();
        verify(mapper, never()).toDomain(any());
    }

    @Test
    void testObtenerUsuariosPorRol_Tecnico_ConResultados() {
        // Arrange
        RolUsuario rol = RolUsuario.TECNICO;

        UsuarioEntity entity2 = new UsuarioEntity();
        entity2.setId(2L);
        entity2.setEmail("pedro.lopez@telconova.com");
        entity2.setRol(RolUsuario.TECNICO);
        entity2.setActivo(true);

        Email email2 = Email.de("pedro.lopez@telconova.com");

        Usuario usuario2 = Usuario.builder()
                .id(2L)
                .email(email2)
                .rol(RolUsuario.TECNICO)
                .activo(true)
                .build();

        List<UsuarioEntity> entities = Arrays.asList(usuarioEntity, entity2);

        when(jpaRepository.findByRolAndActivoTrue(rol)).thenReturn(entities);
        when(mapper.toDomain(usuarioEntity)).thenReturn(usuarioDominio);
        when(mapper.toDomain(entity2)).thenReturn(usuario2);

        // Act
        List<Usuario> resultado = usuarioRepository.obtenerUsuariosPorRol(rol);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(u -> u.getRol() == RolUsuario.TECNICO));
        assertTrue(resultado.stream().allMatch(Usuario::isActivo));

        verify(jpaRepository).findByRolAndActivoTrue(rol);
        verify(mapper, times(2)).toDomain(any(UsuarioEntity.class));
    }

    @Test
    void testObtenerUsuariosPorRol_Administrador_ConResultados() {
        // Arrange
        RolUsuario rol = RolUsuario.ADMIN;

        UsuarioEntity entityAdmin = new UsuarioEntity();
        entityAdmin.setId(3L);
        entityAdmin.setEmail("admin@telconova.com");
        entityAdmin.setRol(RolUsuario.ADMIN);
        entityAdmin.setActivo(true);

        Email emailAdmin = Email.de("admin@telconova.com");

        Usuario usuarioAdmin = Usuario.builder()
                .id(3L)
                .email(emailAdmin)
                .rol(RolUsuario.ADMIN)
                .activo(true)
                .build();

        List<UsuarioEntity> entities = Collections.singletonList(entityAdmin);

        when(jpaRepository.findByRolAndActivoTrue(rol)).thenReturn(entities);
        when(mapper.toDomain(entityAdmin)).thenReturn(usuarioAdmin);

        // Act
        List<Usuario> resultado = usuarioRepository.obtenerUsuariosPorRol(rol);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(RolUsuario.ADMIN, resultado.get(0).getRol());

        verify(jpaRepository).findByRolAndActivoTrue(rol);
        verify(mapper).toDomain(entityAdmin);
    }

    @Test
    void testObtenerUsuariosPorRol_Cliente_ConResultados() {
        // Arrange
        RolUsuario rol = RolUsuario.TECNICO;

        UsuarioEntity entityCliente = new UsuarioEntity();
        entityCliente.setId(4L);
        entityCliente.setEmail("cliente@telconova.com");
        entityCliente.setRol(RolUsuario.TECNICO);
        entityCliente.setActivo(true);

        Email emailCliente = Email.de("cliente@telconova.com");

        Usuario usuarioCliente = Usuario.builder()
                .id(4L)
                .email(emailCliente)
                .rol(RolUsuario.TECNICO)
                .activo(true)
                .build();

        List<UsuarioEntity> entities = Collections.singletonList(entityCliente);

        when(jpaRepository.findByRolAndActivoTrue(rol)).thenReturn(entities);
        when(mapper.toDomain(entityCliente)).thenReturn(usuarioCliente);

        // Act
        List<Usuario> resultado = usuarioRepository.obtenerUsuariosPorRol(rol);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(RolUsuario.TECNICO, resultado.get(0).getRol());
    }

    @Test
    void testObtenerUsuariosPorRol_SinResultados() {
        // Arrange
        RolUsuario rol = RolUsuario.ADMIN;
        when(jpaRepository.findByRolAndActivoTrue(rol)).thenReturn(Collections.emptyList());

        // Act
        List<Usuario> resultado = usuarioRepository.obtenerUsuariosPorRol(rol);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(jpaRepository).findByRolAndActivoTrue(rol);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    void testObtenerUsuariosPorRol_SoloUsuariosActivos() {
        // Arrange
        RolUsuario rol = RolUsuario.TECNICO;

        // El repository solo debe retornar usuarios activos
        List<UsuarioEntity> entities = Collections.singletonList(usuarioEntity);

        when(jpaRepository.findByRolAndActivoTrue(rol)).thenReturn(entities);
        when(mapper.toDomain(usuarioEntity)).thenReturn(usuarioDominio);

        // Act
        List<Usuario> resultado = usuarioRepository.obtenerUsuariosPorRol(rol);

        // Assert
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        assertTrue(resultado.stream().allMatch(Usuario::isActivo));

        verify(jpaRepository).findByRolAndActivoTrue(rol);
    }

    @Test
    void testObtenerUsuariosPorRol_TodosLosRoles() {
        // Arrange - Verificar que funciona para todos los roles del enum
        for (RolUsuario rol : RolUsuario.values()) {
            when(jpaRepository.findByRolAndActivoTrue(rol)).thenReturn(Collections.emptyList());

            // Act
            List<Usuario> resultado = usuarioRepository.obtenerUsuariosPorRol(rol);

            // Assert
            assertNotNull(resultado);
        }

        // Verificar que se llamó para cada rol
        verify(jpaRepository, times(RolUsuario.values().length))
                .findByRolAndActivoTrue(any(RolUsuario.class));
    }

    @Test
    void testBuscarPorEmail_VerificaMapeo() {
        // Arrange
        String email = "juan.perez@telconova.com";
        when(jpaRepository.findByEmail(email)).thenReturn(Optional.of(usuarioEntity));
        when(mapper.toDomain(usuarioEntity)).thenReturn(usuarioDominio);

        // Act
        Optional<Usuario> resultado = usuarioRepository.buscarPorEmail(email);

        // Assert
        assertTrue(resultado.isPresent());
        Usuario usuario = resultado.get();

        // Verificar que todos los campos se mapearon correctamente
        assertNotNull(usuario.getId());
        assertNotNull(usuario.getEmail());
        assertNotNull(usuario.getNombreCompleto());
        assertNotNull(usuario.getRol());
        assertNotNull(usuario.getContrasenaEncriptada());

        verify(mapper).toDomain(usuarioEntity);
    }

    @Test
    void testGuardar_VerificaFlujoCompleto() {
        // Arrange
        when(mapper.toEntity(usuarioDominio)).thenReturn(usuarioEntity);
        when(jpaRepository.save(usuarioEntity)).thenReturn(usuarioEntity);
        when(mapper.toDomain(usuarioEntity)).thenReturn(usuarioDominio);

        // Act
        Usuario resultado = usuarioRepository.guardar(usuarioDominio);

        // Assert
        assertNotNull(resultado);

        // Verificar el orden de las operaciones
        verify(mapper).toEntity(usuarioDominio);
        verify(jpaRepository).save(usuarioEntity);
        verify(mapper).toDomain(usuarioEntity);

        // Verificar que no se llamaron otros métodos
        verifyNoMoreInteractions(mapper);
        verifyNoMoreInteractions(jpaRepository);
    }
}

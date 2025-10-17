package com.telconova.supportsuite.infraestructura.adaptadores.salida.almacenamiento;

import com.cloudinary.*;
import com.telconova.supportsuite.compartido.constantes.ConfiguracionConstantes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para CloudinaryAlmacenamientoImpl")
class CloudinaryAlmacenamientoImplTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @Mock
    private Api api;

    @Mock
    private Url url;

    @Mock
    private MultipartFile archivo;

    private CloudinaryAlmacenamientoImpl almacenamiento;

    private static final String CLOUD_NAME = "test-cloud";
    private static final String API_KEY = "test-key";
    private static final String API_SECRET = "test-secret";
    private static final String CARPETA_BASE = "test-folder";

    @BeforeEach
    void setUp() {
        try (MockedConstruction<Cloudinary> mockedCloudinary = mockConstruction(Cloudinary.class,
                (mock, context) -> {
                    when(mock.uploader()).thenReturn(uploader);
                    when(mock.api()).thenReturn(api);
                    when(mock.url()).thenReturn(url);
                })) {

            almacenamiento = new CloudinaryAlmacenamientoImpl(
                    CLOUD_NAME, API_KEY, API_SECRET, CARPETA_BASE);

            cloudinary = mockedCloudinary.constructed().get(0);
        }

        when(cloudinary.uploader()).thenReturn(uploader);
        when(cloudinary.api()).thenReturn(api);
        when(cloudinary.url()).thenReturn(url);
    }

    @Test
    void testGuardarArchivo_Exitoso() throws IOException {
        // Arrange
        String nombreArchivo = "test-image.jpg";
        String carpeta = "evidencias";
        byte[] contenido = "imagen de prueba".getBytes();

        when(archivo.getOriginalFilename()).thenReturn(nombreArchivo);
        when(archivo.getContentType()).thenReturn("image/jpeg");
        when(archivo.getSize()).thenReturn(1024L);
        when(archivo.isEmpty()).thenReturn(false);
        when(archivo.getBytes()).thenReturn(contenido);

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("public_id", "test-folder/evidencias/test-image");
        resultado.put("secure_url", "https://cloudinary.com/test-image.jpg");
        resultado.put("bytes", 1024L);

        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(resultado);

        // Act
        String publicId = almacenamiento.guardarArchivo(archivo, carpeta);

        // Assert
        assertNotNull(publicId);
        assertEquals("test-folder/evidencias/test-image", publicId);
        verify(uploader).upload(eq(contenido), anyMap());
    }

    @Test
    void testGuardarArchivo_ArchivoVacio_LanzaExcepcion() throws IOException {
        // Arrange
        when(archivo.isEmpty()).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> almacenamiento.guardarArchivo(archivo, "carpeta")
        );

        assertEquals("El archivo no puede estar vacío", exception.getMessage());
        verify(uploader, never()).upload(any(), anyMap());
    }

    @Test
    void testGuardarArchivo_ArchivoNulo_LanzaExcepcion() {
        // Act & Assert
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> almacenamiento.guardarArchivo(null, "carpeta")
        );

        assertEquals("Cannot invoke \"org.springframework.web.multipart.MultipartFile.getOriginalFilename()\" because \"archivo\" is null", exception.getMessage());
    }

    @Test
    void testGuardarArchivo_TipoMimeNoPermitido_LanzaExcepcion() throws IOException {
        // Arrange
        when(archivo.isEmpty()).thenReturn(false);
        when(archivo.getContentType()).thenReturn("application/exe");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> almacenamiento.guardarArchivo(archivo, "carpeta")
        );

        assertTrue(exception.getMessage().contains("Tipo de archivo no permitido"));
        verify(uploader, never()).upload(any(), anyMap());
    }

    @Test
    void testGuardarArchivo_ArchivoMuyGrande_LanzaExcepcion() throws IOException {
        // Arrange
        when(archivo.isEmpty()).thenReturn(false);
        when(archivo.getContentType()).thenReturn("image/jpeg");
        when(archivo.getSize()).thenReturn(ConfiguracionConstantes.TAMANO_MAXIMO_ARCHIVO + 1);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> almacenamiento.guardarArchivo(archivo, "carpeta")
        );

        assertTrue(exception.getMessage().contains("Archivo demasiado grande"));
        verify(uploader, never()).upload(any(), anyMap());
    }

    @Test
    void testGuardarArchivo_ErrorIO_LanzaRuntimeException() throws IOException {
        // Arrange
        when(archivo.isEmpty()).thenReturn(false);
        when(archivo.getContentType()).thenReturn("image/jpeg");
        when(archivo.getSize()).thenReturn(1024L);
        when(archivo.getOriginalFilename()).thenReturn("test.jpg");
        when(archivo.getBytes()).thenThrow(new IOException("Error de lectura"));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> almacenamiento.guardarArchivo(archivo, "carpeta")
        );

        assertTrue(exception.getMessage().contains("Error al guardar archivo en Cloudinary"));
        assertInstanceOf(IOException.class, exception.getCause());
    }

    @Test
    void testEliminarArchivo_Exitoso() throws IOException {
        // Arrange
        String rutaArchivo = "test-folder/test-image";
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("result", "ok");

        when(uploader.destroy(eq(rutaArchivo), anyMap())).thenReturn(resultado);

        // Act
        assertDoesNotThrow(() -> almacenamiento.eliminarArchivo(rutaArchivo));

        // Assert
        verify(uploader).destroy(eq(rutaArchivo), anyMap());
    }

    @Test
    void testEliminarArchivo_NoEncontrado() throws IOException {
        // Arrange
        String rutaArchivo = "test-folder/inexistente";
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("result", "not found");

        when(uploader.destroy(eq(rutaArchivo), anyMap())).thenReturn(resultado);

        // Act
        assertDoesNotThrow(() -> almacenamiento.eliminarArchivo(rutaArchivo));

        // Assert
        verify(uploader).destroy(eq(rutaArchivo), anyMap());
    }

    @Test
    void testArchivoExiste_Falso() throws Exception {
        // Arrange
        String rutaArchivo = "test-folder/inexistente";
        when(api.resource(eq(rutaArchivo), anyMap()))
                .thenThrow(new Exception("Not found"));

        // Act
        boolean existe = almacenamiento.archivoExiste(rutaArchivo);

        // Assert
        assertFalse(existe);
    }

    @Test
    void testObtenerUrlPublica_ConRutaValida() {
        // Arrange
        String rutaArchivo = "test-folder/test-image";
        String urlEsperada = "https://cloudinary.com/test-image-optimized.jpg";

        when(url.transformation(any(Transformation.class))).thenReturn(url);
        when(url.secure(true)).thenReturn(url);
        when(url.generate(rutaArchivo)).thenReturn(urlEsperada);

        // Act
        String urlObtenida = almacenamiento.obtenerUrlPublica(rutaArchivo);

        // Assert
        assertEquals(urlEsperada, urlObtenida);
        verify(url).secure(true);
        verify(url).generate(rutaArchivo);
    }

    @Test
    void testObtenerUrlPublica_ConRutaNula_RetornaNulo() {
        // Act
        String url1 = almacenamiento.obtenerUrlPublica(null);

        // Assert
        assertNull(url1);
    }

    @Test
    void testObtenerUrlPublica_ConRutaVacia_RetornaNulo() {
        // Act
        String url1 = almacenamiento.obtenerUrlPublica("   ");

        // Assert
        assertNull(url1);
    }

    @Test
    void testEsTipoMimePermitido_TiposValidos() {
        // Act & Assert
        assertTrue(almacenamiento.esTipoMimePermitido("image/jpeg"));
        assertTrue(almacenamiento.esTipoMimePermitido("image/png"));
    }

    @Test
    void testEsTipoMimePermitido_TipoNoPermitido() {
        // Act & Assert
        assertFalse(almacenamiento.esTipoMimePermitido("application/pdf"));
        assertFalse(almacenamiento.esTipoMimePermitido("video/mp4"));
    }

    @Test
    void testEsTipoMimePermitido_TipoNulo() {
        // Act & Assert
        assertFalse(almacenamiento.esTipoMimePermitido(null));
    }

    @Test
    void testObtenerUrlConTransformacion() {
        // Arrange
        String publicId = "test-folder/test-image";
        int ancho = 300;
        int alto = 200;
        String urlEsperada = "https://cloudinary.com/w_300,h_200/test-image.jpg";

        when(url.transformation(any(Transformation.class))).thenReturn(url);
        when(url.secure(true)).thenReturn(url);
        when(url.generate(publicId)).thenReturn(urlEsperada);

        // Act
        String urlObtenida = almacenamiento.obtenerUrlConTransformacion(publicId, ancho, alto);

        // Assert
        assertEquals(urlEsperada, urlObtenida);
        verify(url).secure(true);
        verify(url).generate(publicId);
    }

    @Test
    void testObtenerMiniatura() {
        // Arrange
        String publicId = "test-folder/test-image";
        String urlEsperada = "https://cloudinary.com/w_150,h_150/test-image.jpg";

        when(url.transformation(any(Transformation.class))).thenReturn(url);
        when(url.secure(true)).thenReturn(url);
        when(url.generate(publicId)).thenReturn(urlEsperada);

        // Act
        String urlMiniatura = almacenamiento.obtenerMiniatura(publicId);

        // Assert
        assertEquals(urlEsperada, urlMiniatura);
        verify(url).secure(true);
        verify(url).generate(publicId);
    }
}

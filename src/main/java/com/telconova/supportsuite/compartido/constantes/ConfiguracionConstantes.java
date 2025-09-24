package com.telconova.supportsuite.compartido.constantes;

public class ConfiguracionConstantes {

    private ConfiguracionConstantes() {
    }

    // Constantes de JWT
    public static final String JWT_HEADER = "Authorization";
    public static final String JWT_PREFIX = "Bearer ";
    public static final String JWT_CLAIM_ROL = "rol";
    public static final String JWT_CLAIM_EMAIL = "email";

    // Constantes de archivos
    public static final long TAMAANO_MAXIMO_ARCHIVO = 10 * 1024 * 1024; // 10MB
    public static final String[] TIPOS_MIME_PERMITIDOS = {"image/jpeg", "image/jpg", "image/png", "image/svg"};
    public static final String DIRECTORIO_UPLOADS = "uploads";
    public static final String SUBDIRECTORIO_EVIDENCIAS = "evidencias";

    // Constantes de validación
    public static final String DOMINIO_EMAIL_PERMITIDO = "@telconova.com";
    public static final int LONGITUD_MAXIMA_COMENTARIO = 500;
    public static final int LONGITUD_MAXIMA_TITULO = 255;
    public static final int LONGITUD_MAXIMA_NOMBRE = 255;
    public static final int LONGITUD_MAXIMA_DIRECCION = 500;

    // Constantes de paginación
    public static final int TAMAANO_PAGINA_DEFAULT = 20;
    public static final int TAMAANO_MAXIMO_PAGINA = 100;
    public static final int LIMITE_BUSQUEDA_MATERIALES = 10;

    // Constantes de tiempo
    public static final long EXPIRACION_TOKEN_MILLIS = 24 * 60 * 60 * 1000; // 24 horas
    public static final long EXPIRACION_REFRESH_TOKEN_MILLIS = 7 * 24 * 60 * 60 * 1000; // 7 días
    public static final int DIAS_LIMITE_ORDEN_VENCIDA = 7;

    // Constantes de formato
    public static final String FORMATO_FECHA_API = "yyyy-MM-dd HH:mm:ss";
    public static final String ZONA_HORARIA_COLOMBIA = "America/Bogota";

    // Constantes de cache
    public static final String CACHE_USUARIOS = "usuarios";
    public static final String CACHE_MATERIALES = "materiales";
    public static final String CACHE_ORDENES = "ordenes";

    // Constantes de métricas
    public static final String METRIC_ORDENES_ACTIVAS = "telconova.ordenes.activas";
    public static final String METRIC_ORDENES_FINALIZADAS = "telconova.ordenes.finalizadas.total";
    public static final String METRIC_MATERIALES_UTILIZADOS = "telconova.materiales.utilizados.total";
    public static final String METRIC_EVIDENCIAS_SUBIDAS = "telconova.evidencias.subidas.total";
    public static final String METRIC_USUARIOS_ACTIVOS = "telconova.usuarios.activos";
}

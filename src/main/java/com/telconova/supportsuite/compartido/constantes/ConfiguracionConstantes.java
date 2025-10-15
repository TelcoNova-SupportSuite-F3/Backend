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
    public static final long TAMANO_MAXIMO_ARCHIVO = 10L * 1024 * 1024; // 10MB
    public static final String[] TIPOS_MIME_PERMITIDOS = {"image/jpeg", "image/jpg", "image/png", "image/svg"};
    public static final String DIRECTORIO_UPLOADS = "uploads";
    public static final String SUBDIRECTORIO_EVIDENCIAS = "evidencias";

    // Constantes de validación
    public static final String DOMINIO_EMAIL_PERMITIDO = "@telconova.com";
    public static final int LONGITUD_MAXIMA_COMENTARIO = 500;
    public static final int LONGITUD_MAXIMA_TITULO = 255;

    // Constantes de tiempo
    public static final long EXPIRACION_TOKEN_MILLIS = 24L * 60 * 60 * 1000; // 24 horas
    public static final int DIAS_LIMITE_ORDEN_VENCIDA = 7;

    // Constantes de formato
    public static final String FORMATO_FECHA_API = "yyyy-MM-dd HH:mm:ss";
    public static final String ZONA_HORARIA_COLOMBIA = "America/Bogota";

    // Constantes de cache
    public static final String CACHE_USUARIOS = "usuarios";
    public static final String CACHE_MATERIALES = "materiales";
    public static final String CACHE_ORDENES = "ordenes";
}

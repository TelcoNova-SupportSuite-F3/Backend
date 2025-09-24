package com.telconova.supportsuite.compartido.constantes;

public class MensajesConstantes {

    private MensajesConstantes() {
        // Utility class
    }

    // Mensajes de autenticación
    public static final String ERROR_CREDENCIALES_INVALIDAS = "Las credenciales proporcionadas son inválidas";
    public static final String ERROR_USUARIO_INACTIVO = "El usuario está inactivo";
    public static final String ERROR_EMAIL_NO_TELCONOVA = "Solo se permiten emails del dominio @telconova.com";
    public static final String ERROR_TOKEN_INVALIDO = "Token de autenticación inválido o expirado";
    public static final String ERROR_TOKEN_EXPIRADO = "El token ha expirado";
    public static final String SUCCESS_LOGIN = "Inicio de sesión exitoso";
    public static final String SUCCESS_LOGOUT = "Sesión cerrada exitosamente";

    // Mensajes de órdenes
    public static final String ERROR_ORDEN_NO_ENCONTRADA = "Orden de trabajo no encontrada";
    public static final String ERROR_ORDEN_NO_AUTORIZADA = "No tiene autorización para acceder a esta orden";
    public static final String ERROR_ESTADO_ORDEN_INVALIDO = "No se puede cambiar al estado solicitado";
    public static final String ERROR_ORDEN_SIN_TECNICO = "La orden no tiene técnico asignado";
    public static final String ERROR_FECHAS_FINALIZACION_REQUERIDAS = "Las fechas de inicio y fin son obligatorias para finalizar";
    public static final String ERROR_EVIDENCIA_REQUERIDA_FINALIZACION = "Se requiere al menos una evidencia para finalizar la orden";
    public static final String SUCCESS_ORDEN_ACTUALIZADA = "Orden actualizada exitosamente";
    public static final String SUCCESS_ORDEN_FINALIZADA = "Orden finalizada exitosamente";

    // Mensajes de evidencias
    public static final String ERROR_EVIDENCIA_NO_ENCONTRADA = "Evidencia no encontrada";
    public static final String ERROR_COMENTARIO_VACIO = "El comentario no puede estar vacío";
    public static final String ERROR_COMENTARIO_MUY_LARGO = "El comentario excede la longitud máxima permitida";
    public static final String ERROR_ARCHIVO_VACIO = "El archivo no puede estar vacío";
    public static final String ERROR_TIPO_ARCHIVO_NO_PERMITIDO = "Tipo de archivo no permitido. Solo se permiten imágenes JPG, JPEG, PNG";
    public static final String ERROR_ARCHIVO_MUY_GRANDE = "El archivo es demasiado grande";
    public static final String ERROR_EVIDENCIA_REQUERIDA = "Se requiere al menos un comentario o una foto como evidencia";
    public static final String SUCCESS_EVIDENCIA_REGISTRADA = "Evidencia registrada exitosamente";
    public static final String SUCCESS_EVIDENCIA_ELIMINADA = "Evidencia eliminada exitosamente";

    // Mensajes de materiales
    public static final String ERROR_MATERIAL_NO_ENCONTRADO = "Material no encontrado";
    public static final String ERROR_MATERIAL_INACTIVO = "El material está inactivo";
    public static final String ERROR_STOCK_INSUFICIENTE = "Stock insuficiente para el material solicitado";
    public static final String ERROR_CANTIDAD_INVALIDA = "La cantidad debe ser mayor a cero";
    public static final String ERROR_MATERIAL_ESTADO_ORDEN = "Solo se pueden agregar materiales cuando la orden está EN_PROCESO";
    public static final String SUCCESS_MATERIAL_AGREGADO = "Material agregado exitosamente a la orden";
    public static final String SUCCESS_MATERIAL_CREADO = "Material creado exitosamente";

    // Mensajes de validación general
    public static final String ERROR_CAMPO_REQUERIDO = "Este campo es obligatorio";
    public static final String ERROR_FORMATO_INVALIDO = "El formato del campo no es válido";
    public static final String ERROR_LONGITUD_MAXIMA_EXCEDIDA = "Se ha excedido la longitud máxima permitida";
    public static final String ERROR_VALOR_DUPLICADO = "El valor ya existe en el sistema";
    public static final String ERROR_ACCESO_DENEGADO = "Acceso denegado. Permisos insuficientes";

    // Mensajes de sistema
    public static final String ERROR_INTERNO_SERVIDOR = "Error interno del servidor. Contacte al administrador";
    public static final String ERROR_SERVICIO_NO_DISPONIBLE = "Servicio temporalmente no disponible";
    public static final String ERROR_BASE_DATOS = "Error de conectividad con la base de datos";
    public static final String SUCCESS_OPERACION_COMPLETADA = "Operación completada exitosamente";

    // Mensajes de archivo
    public static final String ERROR_SUBIDA_ARCHIVO = "Error al subir el archivo";
    public static final String ERROR_ALMACENAMIENTO_NO_DISPONIBLE = "Servicio de almacenamiento no disponible";
    public static final String SUCCESS_ARCHIVO_SUBIDO = "Archivo subido exitosamente";
    public static final String SUCCESS_ARCHIVO_ELIMINADO = "Archivo eliminado exitosamente";
}

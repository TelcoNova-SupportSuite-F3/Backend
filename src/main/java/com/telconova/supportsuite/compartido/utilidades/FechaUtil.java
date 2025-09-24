package com.telconova.supportsuite.compartido.utilidades;

import com.telconova.supportsuite.compartido.constantes.ConfiguracionConstantes;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Utilidades para manejo de fechas
 */
public class FechaUtil {

    private static final DateTimeFormatter FORMATTER_API =
            DateTimeFormatter.ofPattern(ConfiguracionConstantes.FORMATO_FECHA_API);

    private static final ZoneId ZONA_COLOMBIA =
            ZoneId.of(ConfiguracionConstantes.ZONA_HORARIA_COLOMBIA);

    private FechaUtil() {
        // Utility class
    }

    /**
     * Obtiene la fecha actual en zona horaria de Colombia
     */
    public static LocalDateTime ahora() {
        return LocalDateTime.now(ZONA_COLOMBIA);
    }

    /**
     * Formatea fecha para API
     */
    public static String formatearParaApi(LocalDateTime fecha) {
        if (fecha == null) {
            return null;
        }
        return fecha.format(FORMATTER_API);
    }

    /**
     * Parsea fecha desde string de API
     */
    public static LocalDateTime parsearDesdeApi(String fechaStr) {
        if (fechaStr == null || fechaStr.trim().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(fechaStr, FORMATTER_API);
    }

    /**
     * Convierte LocalDateTime a Date
     */
    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZONA_COLOMBIA).toInstant());
    }

    /**
     * Convierte Date a LocalDateTime
     */
    public static LocalDateTime fromDate(Date date) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.ofInstant(date.toInstant(), ZONA_COLOMBIA);
    }

    /**
     * Calcula diferencia en días entre dos fechas
     */
    public static long diasEntre(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(fechaInicio, fechaFin);
    }

    /**
     * Calcula diferencia en horas entre dos fechas
     */
    public static long horasEntre(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            return 0;
        }
        return ChronoUnit.HOURS.between(fechaInicio, fechaFin);
    }

    /**
     * Calcula diferencia en minutos entre dos fechas
     */
    public static long minutosEntre(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(fechaInicio, fechaFin);
    }

    /**
     * Verifica si una fecha está en el pasado
     */
    public static boolean esEnElPasado(LocalDateTime fecha) {
        if (fecha == null) {
            return false;
        }
        return fecha.isBefore(ahora());
    }

    /**
     * Verifica si una fecha está en el futuro
     */
    public static boolean esEnElFuturo(LocalDateTime fecha) {
        if (fecha == null) {
            return false;
        }
        return fecha.isAfter(ahora());
    }

    /**
     * Agrega días a una fecha
     */
    public static LocalDateTime agregarDias(LocalDateTime fecha, int dias) {
        if (fecha == null) {
            return null;
        }
        return fecha.plusDays(dias);
    }

    /**
     * Agrega horas a una fecha
     */
    public static LocalDateTime agregarHoras(LocalDateTime fecha, int horas) {
        if (fecha == null) {
            return null;
        }
        return fecha.plusHours(horas);
    }

    /**
     * Obtiene el inicio del día (00:00:00)
     */
    public static LocalDateTime inicioDia(LocalDateTime fecha) {
        if (fecha == null) {
            return null;
        }
        return fecha.toLocalDate().atStartOfDay();
    }

    /**
     * Obtiene el fin del día (23:59:59)
     */
    public static LocalDateTime finDia(LocalDateTime fecha) {
        if (fecha == null) {
            return null;
        }
        return fecha.toLocalDate().atTime(23, 59, 59);
    }

    /**
     * Verifica si una orden está vencida
     */
    public static boolean ordenEstaVencida(LocalDateTime fechaCreacion) {
        if (fechaCreacion == null) {
            return false;
        }
        long diasTranscurridos = diasEntre(fechaCreacion, ahora());
        return diasTranscurridos > ConfiguracionConstantes.DIAS_LIMITE_ORDEN_VENCIDA;
    }
}

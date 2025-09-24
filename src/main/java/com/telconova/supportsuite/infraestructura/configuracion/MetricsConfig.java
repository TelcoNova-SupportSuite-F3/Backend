package com.telconova.supportsuite.infraestructura.configuracion;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;

/**
 * Configuración de métricas para Grafana
 */
@Configuration
public class MetricsConfig {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags(
                "application", "telconova-supportsuite",
                "version", "1.0.0"
        );
    }

    @Bean
    public Timer ordenesTimer(MeterRegistry meterRegistry) {
        return Timer.builder("telconova.ordenes.procesamiento")
                .description("Tiempo de procesamiento de órdenes")
                .register(meterRegistry);
    }
}
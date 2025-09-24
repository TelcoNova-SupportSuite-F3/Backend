package com.telconova.supportsuite.infraestructura.configuracion;

import com.telconova.supportsuite.aplicacion.puertos.salida.IAlmacenamientoArchivos;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.almacenamiento.CloudinaryAlmacenamientoImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Slf4j
@Configuration
public class StorageConfiguration {

    @Bean
    @Primary
    @ConditionalOnProperty(name = "storage.type", havingValue = "cloudinary", matchIfMissing = false)
    public IAlmacenamientoArchivos cloudinaryAlmacenamientoArchivos(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret,
            @Value("${cloudinary.folder:telconova}") String carpetaBase) {

        log.info("Configurando almacenamiento con Cloudinary");
        return new CloudinaryAlmacenamientoImpl(cloudName, apiKey, apiSecret, carpetaBase);
    }
}

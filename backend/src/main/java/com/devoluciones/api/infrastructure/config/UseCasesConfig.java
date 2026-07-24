package com.devoluciones.api.infrastructure.config;

import com.devoluciones.api.core.domain.port.SolicitudRepositoryPort;
import com.devoluciones.api.core.usecase.solicitudes.CrearSolicitudUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class UseCasesConfig {

    @Bean
    @Transactional
    public CrearSolicitudUseCase crearSolicitudUseCase(SolicitudRepositoryPort solicitudRepositoryPort) {
        return new CrearSolicitudUseCase(solicitudRepositoryPort);
    }
}

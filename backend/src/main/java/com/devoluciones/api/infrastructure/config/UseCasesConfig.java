package com.devoluciones.api.infrastructure.config;

import com.devoluciones.api.core.domain.port.SolicitudRepositoryPort;
import com.devoluciones.api.core.usecase.solicitudes.ConsultarSolicitudesUseCase;
import com.devoluciones.api.core.usecase.solicitudes.GestionarSolicitudesBorradorUseCase;
import com.devoluciones.api.core.usecase.solicitudes.GestionarTransicionesBorradorUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class UseCasesConfig {

    @Bean
    @Transactional
    public GestionarSolicitudesBorradorUseCase gestionarSolicitudesBorradorUseCase(SolicitudRepositoryPort solicitudRepositoryPort) {
        return new GestionarSolicitudesBorradorUseCase(solicitudRepositoryPort);
    }

    @Bean
    @Transactional
    public GestionarTransicionesBorradorUseCase gestionarTransicionesBorradorUseCase(SolicitudRepositoryPort solicitudRepositoryPort) {
        return new GestionarTransicionesBorradorUseCase(solicitudRepositoryPort);
    }

    @Bean
    @Transactional(readOnly = true)
    public ConsultarSolicitudesUseCase consultarSolicitudesUseCase(SolicitudRepositoryPort solicitudRepositoryPort) {
        return new ConsultarSolicitudesUseCase(solicitudRepositoryPort);
    }
}

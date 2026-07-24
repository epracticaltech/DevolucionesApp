package com.devoluciones.api.infrastructure.config;

import com.devoluciones.api.core.domain.port.CargaMasivaRepositoryPort;
import com.devoluciones.api.core.domain.port.FolioGeneratorPort;
import com.devoluciones.api.core.domain.port.SolicitudRepositoryPort;
import com.devoluciones.api.core.usecase.cargas.ProcesarCargaMasivaUseCase;
import com.devoluciones.api.core.usecase.solicitudes.ConsultarSolicitudesUseCase;
import com.devoluciones.api.core.usecase.solicitudes.GestionarSolicitudesBorradorUseCase;
import com.devoluciones.api.core.usecase.solicitudes.GestionarTransicionesBorradorUseCase;
import com.devoluciones.api.core.usecase.solicitudes.GestionarTransicionesFinalesUseCase;
import com.devoluciones.api.core.usecase.solicitudes.GestionarTransicionesRevisionUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class UseCasesConfig {

    @Bean
    @Transactional
    public GestionarSolicitudesBorradorUseCase gestionarSolicitudesBorradorUseCase(
            SolicitudRepositoryPort solicitudRepositoryPort,
            FolioGeneratorPort folioGeneratorPort) {
        return new GestionarSolicitudesBorradorUseCase(solicitudRepositoryPort, folioGeneratorPort);
    }

    @Bean
    @Transactional
    public GestionarTransicionesBorradorUseCase gestionarTransicionesBorradorUseCase(SolicitudRepositoryPort solicitudRepositoryPort) {
        return new GestionarTransicionesBorradorUseCase(solicitudRepositoryPort);
    }

    @Bean
    @Transactional
    public GestionarTransicionesRevisionUseCase gestionarTransicionesRevisionUseCase(SolicitudRepositoryPort solicitudRepositoryPort) {
        return new GestionarTransicionesRevisionUseCase(solicitudRepositoryPort);
    }

    @Bean
    @Transactional
    public GestionarTransicionesFinalesUseCase gestionarTransicionesFinalesUseCase(SolicitudRepositoryPort solicitudRepositoryPort) {
        return new GestionarTransicionesFinalesUseCase(solicitudRepositoryPort);
    }

    @Bean
    @Transactional(readOnly = true)
    public ConsultarSolicitudesUseCase consultarSolicitudesUseCase(SolicitudRepositoryPort solicitudRepositoryPort) {
        return new ConsultarSolicitudesUseCase(solicitudRepositoryPort);
    }

    @Bean
    @Transactional
    public ProcesarCargaMasivaUseCase procesarCargaMasivaUseCase(
            CargaMasivaRepositoryPort cargaMasivaRepositoryPort,
            SolicitudRepositoryPort solicitudRepositoryPort,
            FolioGeneratorPort folioGeneratorPort) {
        return new ProcesarCargaMasivaUseCase(cargaMasivaRepositoryPort, solicitudRepositoryPort, folioGeneratorPort);
    }
}

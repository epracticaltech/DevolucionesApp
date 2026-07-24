package com.devoluciones.api.infrastructure.config;

import com.devoluciones.api.core.domain.port.SolicitudRepositoryPort;
import com.devoluciones.api.core.usecase.solicitudes.ActualizarSolicitudUseCase;
import com.devoluciones.api.core.usecase.solicitudes.CrearSolicitudUseCase;
import com.devoluciones.api.core.usecase.solicitudes.GestionarTransicionesBorradorUseCase;
import com.devoluciones.api.core.usecase.solicitudes.ListarSolicitudesUseCase;
import com.devoluciones.api.core.usecase.solicitudes.ObtenerSolicitudPorIdUseCase;
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

    @Bean
    @Transactional
    public ActualizarSolicitudUseCase actualizarSolicitudUseCase(SolicitudRepositoryPort solicitudRepositoryPort) {
        return new ActualizarSolicitudUseCase(solicitudRepositoryPort);
    }

    @Bean
    @Transactional
    public GestionarTransicionesBorradorUseCase gestionarTransicionesBorradorUseCase(SolicitudRepositoryPort solicitudRepositoryPort) {
        return new GestionarTransicionesBorradorUseCase(solicitudRepositoryPort);
    }

    @Bean
    @Transactional(readOnly = true)
    public ObtenerSolicitudPorIdUseCase obtenerSolicitudPorIdUseCase(SolicitudRepositoryPort solicitudRepositoryPort) {
        return new ObtenerSolicitudPorIdUseCase(solicitudRepositoryPort);
    }

    @Bean
    @Transactional(readOnly = true)
    public ListarSolicitudesUseCase listarSolicitudesUseCase(SolicitudRepositoryPort solicitudRepositoryPort) {
        return new ListarSolicitudesUseCase(solicitudRepositoryPort);
    }
}

package com.devoluciones.api.core.domain.port;

import com.devoluciones.api.core.domain.models.cargas.CargaMasiva;
import com.devoluciones.api.core.domain.models.cargas.DetalleCargaError;

import java.util.List;
import java.util.Optional;

public interface CargaMasivaRepositoryPort {

    CargaMasiva guardar(CargaMasiva cargaMasiva);

    Optional<CargaMasiva> buscarPorId(Long id);

    void guardarErrores(List<DetalleCargaError> errores);
}

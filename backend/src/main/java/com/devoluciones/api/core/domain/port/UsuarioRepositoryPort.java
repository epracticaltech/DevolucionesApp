package com.devoluciones.api.core.domain.port;

import com.devoluciones.api.core.domain.models.Usuario;

import java.util.Optional;

public interface UsuarioRepositoryPort {
    Optional<Usuario> buscarPorUsername(String username);
}

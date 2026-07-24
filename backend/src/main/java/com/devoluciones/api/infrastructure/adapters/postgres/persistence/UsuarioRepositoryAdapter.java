package com.devoluciones.api.infrastructure.adapters.postgres.persistence;

import com.devoluciones.api.core.domain.models.Usuario;
import com.devoluciones.api.core.domain.port.UsuarioRepositoryPort;
import com.devoluciones.api.infrastructure.adapters.postgres.entities.UsuarioEntity;
import com.devoluciones.api.infrastructure.adapters.postgres.repositories.UsuarioJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Transactional(readOnly = true)
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

    private final UsuarioJpaRepository usuarioJpaRepository;

    public UsuarioRepositoryAdapter(UsuarioJpaRepository usuarioJpaRepository) {
        this.usuarioJpaRepository = usuarioJpaRepository;
    }

    @Override
    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioJpaRepository.findByUsername(username).map(this::toDomain);
    }

    private Usuario toDomain(UsuarioEntity entity) {
        return Usuario.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .password(entity.getPassword())
                .mail(entity.getMail())
                .rol(entity.getRol())
                .build();
    }
}

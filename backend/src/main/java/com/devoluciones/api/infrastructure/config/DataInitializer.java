package com.devoluciones.api.infrastructure.config;

import com.devoluciones.api.infrastructure.adapters.postgres.entities.UsuarioEntity;
import com.devoluciones.api.infrastructure.adapters.postgres.repositories.UsuarioJpaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioJpaRepository usuarioJpaRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioJpaRepository usuarioJpaRepository, PasswordEncoder passwordEncoder) {
        this.usuarioJpaRepository = usuarioJpaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String encodedPassword = passwordEncoder.encode("password123");

        crearOActualizarUsuario("analista1", "analista1@devoluciones.cl", encodedPassword, "ANALISTA");
        crearOActualizarUsuario("analista2", "analista2@devoluciones.cl", encodedPassword, "ANALISTA");
        crearOActualizarUsuario("supervisor1", "supervisor1@devoluciones.cl", encodedPassword, "SUPERVISOR");
        crearOActualizarUsuario("supervisor2", "supervisor2@devoluciones.cl", encodedPassword, "SUPERVISOR");
    }

    private void crearOActualizarUsuario(String username, String email, String encodedPassword, String rol) {
        UsuarioEntity usuario = usuarioJpaRepository.findByUsername(username)
                .orElse(UsuarioEntity.builder()
                        .username(username)
                        .mail(email)
                        .rol(rol)
                        .build());

        usuario.setPassword(encodedPassword);
        usuarioJpaRepository.save(usuario);
    }
}

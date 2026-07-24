package com.devoluciones.api.infrastructure.config;

import com.devoluciones.api.infrastructure.adapters.postgres.entities.UsuarioEntity;
import com.devoluciones.api.infrastructure.adapters.postgres.repositories.UsuarioJpaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
@DependsOn("flyway")
public class DataInitializer implements CommandLineRunner {

    private final UsuarioJpaRepository usuarioJpaRepository;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

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

        // Ajustar secuencia de folios al total de solicitudes para evitar duplicidad de folios
        try {
            Number count = (Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM solicitudes").getSingleResult();
            if (count != null && count.longValue() > 0) {
                long nextVal = count.longValue();
                try {
                    entityManager.createNativeQuery("SELECT setval('seq_folio_solicitud', " + nextVal + ")").getSingleResult();
                } catch (Exception e1) {
                    try {
                        entityManager.createNativeQuery("ALTER SEQUENCE seq_folio_solicitud RESTART WITH " + (nextVal + 1)).executeUpdate();
                    } catch (Exception ignored) {
                    }
                }
            }
        } catch (Exception ignored) {
        }
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

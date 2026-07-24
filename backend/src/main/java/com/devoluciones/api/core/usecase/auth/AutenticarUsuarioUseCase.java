package com.devoluciones.api.core.usecase.auth;

import com.devoluciones.api.core.domain.models.Usuario;
import com.devoluciones.api.core.domain.port.UsuarioRepositoryPort;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AutenticarUsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AutenticarUsuarioUseCase(UsuarioRepositoryPort usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario autenticar(String username, String password) {
        Usuario usuario = usuarioRepository.buscarPorUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas: nombre de usuario o contraseña incorrectos."));

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas: nombre de usuario o contraseña incorrectos.");
        }

        return usuario;
    }
}

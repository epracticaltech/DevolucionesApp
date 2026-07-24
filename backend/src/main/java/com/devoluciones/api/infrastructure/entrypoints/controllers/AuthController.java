package com.devoluciones.api.infrastructure.entrypoints.controllers;

import com.devoluciones.api.core.domain.models.Usuario;
import com.devoluciones.api.core.usecase.auth.AutenticarUsuarioUseCase;
import com.devoluciones.api.infrastructure.entrypoints.dto.request.LoginRequestDTO;
import com.devoluciones.api.infrastructure.entrypoints.dto.response.usuario.AuthResponseDTO;
import com.devoluciones.api.infrastructure.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(
            AutenticarUsuarioUseCase autenticarUsuarioUseCase,
            JwtTokenProvider jwtTokenProvider) {
        this.autenticarUsuarioUseCase = autenticarUsuarioUseCase;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginDTO) {
        Usuario usuario = autenticarUsuarioUseCase.autenticar(loginDTO.username(), loginDTO.password());

        String token = jwtTokenProvider.generarToken(usuario.getUsername(), usuario.getRol());

        AuthResponseDTO responseDTO = new AuthResponseDTO(
                token,
                jwtTokenProvider.getExpirationMs(),
                usuario.getRol(),
                usuario.getUsername(),
                usuario.getMail()
        );

        return ResponseEntity.ok(responseDTO);
    }
}

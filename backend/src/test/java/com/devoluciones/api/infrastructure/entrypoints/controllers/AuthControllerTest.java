package com.devoluciones.api.infrastructure.entrypoints.controllers;

import com.devoluciones.api.infrastructure.adapters.postgres.entities.UsuarioEntity;
import com.devoluciones.api.infrastructure.adapters.postgres.repositories.UsuarioJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AuthControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UsuarioJpaRepository usuarioJpaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        usuarioJpaRepository.deleteAll();

        UsuarioEntity analista = UsuarioEntity.builder()
                .username("analista1")
                .password(passwordEncoder.encode("password123"))
                .mail("analista1@devoluciones.cl")
                .rol("ANALISTA")
                .build();

        usuarioJpaRepository.save(analista);
    }

    @Test
    @DisplayName("Debe autenticar exitosamente y retornar token JWT con rol")
    void loginExitoso_retornaTokenYRol() throws Exception {
        Map<String, String> credenciales = Map.of(
                "username", "analista1",
                "password", "password123"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credenciales)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.rol").value("ANALISTA"))
                .andExpect(jsonPath("$.username").value("analista1"))
                .andExpect(jsonPath("$.expires_in").value(3600));
    }

    @Test
    @DisplayName("Debe retornar 401 Unauthorized cuando la contraseña es incorrecta")
    void loginPasswordIncorrecto_retorna401() throws Exception {
        Map<String, String> credenciales = Map.of(
                "username", "analista1",
                "password", "password_incorrecto"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credenciales)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}

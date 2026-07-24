package com.devoluciones.api.infrastructure.security;

import com.devoluciones.api.core.usecase.solicitudes.GestionarTransicionesRevisionUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class RbacSecurityTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private GestionarTransicionesRevisionUseCase gestionarTransicionesRevisionUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @DisplayName("Debe retornar 401 Unauthorized cuando se realiza una petición sin token JWT")
    void sinToken_debeRetornar401() throws Exception {
        mockMvc.perform(post("/api/v1/solicitudes/1/aprobar"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("No Autorizado (401)"));
    }

    @Test
    @WithMockUser(username = "analista1", roles = {"ANALISTA"})
    @DisplayName("Debe retornar 403 Forbidden cuando un ANALISTA intenta aprobar una solicitud (Regla R2)")
    void analistaIntentaAprobar_debeRetornar403() throws Exception {
        mockMvc.perform(post("/api/v1/solicitudes/1/aprobar"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Acceso Denegado (403)"));
    }

    @Test
    @WithMockUser(username = "analista1", roles = {"ANALISTA"})
    @DisplayName("Debe retornar 403 Forbidden cuando un ANALISTA intenta rechazar una solicitud (Regla R2)")
    void analistaIntentaRechazar_debeRetornar403() throws Exception {
        Map<String, String> body = Map.of("motivoRechazo", "Monto no coincide");

        mockMvc.perform(post("/api/v1/solicitudes/1/rechazar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(username = "analista1", roles = {"ANALISTA"})
    @DisplayName("Debe retornar 403 Forbidden cuando un ANALISTA intenta pagar una solicitud (Regla R2)")
    void analistaIntentaPagar_debeRetornar403() throws Exception {
        mockMvc.perform(post("/api/v1/solicitudes/1/pagar"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}

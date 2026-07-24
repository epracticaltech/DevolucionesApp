package com.devoluciones.api.core.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Usuario {
    private final Long id;
    private final String username;
    private final String password;
    private final String mail;
    private final String rol;
}

package com.devoluciones.api.infrastructure.entrypoints.dto.request;

import com.devoluciones.api.infrastructure.entrypoints.validation.ValidRut;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CrearSolicitudRequestDTO(
        @NotBlank(message = "El RUT del cliente es obligatorio")
        @ValidRut(message = "El RUT del cliente no es válido según el algoritmo Módulo 11")
        String rutCliente,

        @NotBlank(message = "El nombre del cliente es obligatorio")
        String nombreCliente,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0 CLP")
        @DecimalMax(value = "10000000.00", message = "El monto no puede superar los 10.000.000 CLP")
        BigDecimal monto,

        @NotBlank(message = "El banco de destino es obligatorio")
        String bancoDestino,

        @NotBlank(message = "La cuenta de destino es obligatoria")
        String cuentaDestino,

        String referenciaBanco,

        @NotNull(message = "El usuario que realiza la operación es obligatorio")
        @Valid
        UsuarioAutenticadoDto usuario
) {}

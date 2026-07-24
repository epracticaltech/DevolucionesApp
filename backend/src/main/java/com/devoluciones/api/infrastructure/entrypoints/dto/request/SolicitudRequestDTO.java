package com.devoluciones.api.infrastructure.entrypoints.dto.request;

import com.devoluciones.api.infrastructure.entrypoints.validation.ValidRut;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO unificado de entrada para crear y editar solicitudes en estado BORRADOR
 * (POST /api/v1/solicitudes y PUT /api/v1/solicitudes/{id}).
 */
public record SolicitudRequestDTO(

        @NotBlank(message = "El RUT del cliente es obligatorio")
        @ValidRut
        String rutCliente,

        @NotBlank(message = "El nombre del cliente es obligatorio")
        String nombreCliente,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser positivo y mayor a 0")
        @DecimalMax(value = "10000000.00", message = "El monto no puede superar 10.000.000 CLP")
        BigDecimal monto,

        @NotBlank(message = "El banco destino es obligatorio")
        String bancoDestino,

        @NotBlank(message = "La cuenta destino es obligatoria")
        String cuentaDestino,

        @NotBlank(message = "La referencia del banco es obligatoria")
        String referenciaBanco
) {
}

package com.devoluciones.api.core.domain.models.enums;

import com.devoluciones.api.core.domain.exceptions.TransicionInvalidaException;

import java.util.Set;

/**
 * Máquina de Estados Cohesiva (State Pattern en Enum).
 * Define las reglas R1 y R2 de la prueba técnica.
 */
public enum EstadoSolicitud {

    BORRADOR {
        @Override
        public Set<EstadoSolicitud> transicionesPermitidas() {
            return Set.of(EN_REVISION, ANULADA);
        }
    },

    EN_REVISION {
        @Override
        public Set<EstadoSolicitud> transicionesPermitidas() {
            return Set.of(APROBADA, RECHAZADA);
        }
    },

    APROBADA {
        @Override
        public Set<EstadoSolicitud> transicionesPermitidas() {
            return Set.of(PAGADA);
        }
    },

    RECHAZADA {
        @Override
        public Set<EstadoSolicitud> transicionesPermitidas() {
            return Set.of(BORRADOR); // Reabrir (máximo 1 vez, validado en negocio R4)
        }
    },

    PAGADA {
        @Override
        public Set<EstadoSolicitud> transicionesPermitidas() {
            return Set.of(); // Estado final inmutable
        }
    },

    ANULADA {
        @Override
        public Set<EstadoSolicitud> transicionesPermitidas() {
            return Set.of(); // Estado final inmutable
        }
    };

    /**
     * Retorna los estados siguientes a los que este estado puede transicionar.
     */
    public abstract Set<EstadoSolicitud> transicionesPermitidas();

    /**
     * Valida si la transición hacia el nuevo estado es permitida segun R1.
     * Si no es permitida, lanza TransicionInvalidaException (HTTP 409).
     */
    public void validarTransicionHacia(EstadoSolicitud nuevoEstado) {
        if (!transicionesPermitidas().contains(nuevoEstado)) {
            throw new TransicionInvalidaException(
                    String.format("Transición inválida: no es posible pasar de %s a %s.", this.name(),
                            nuevoEstado.name()));
        }
    }

}

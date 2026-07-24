package com.devoluciones.api.core.domain.models.enums;

import com.devoluciones.api.core.domain.exceptions.TransicionInvalidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EstadoSolicitudTest {

    @Test
    @DisplayName("R1: Transiciones válidas desde BORRADOR a EN_REVISION y ANULADA")
    void borradorPermiteTransicionesValidas() {
        assertDoesNotThrow(() -> EstadoSolicitud.BORRADOR.validarTransicionHacia(EstadoSolicitud.EN_REVISION));
        assertDoesNotThrow(() -> EstadoSolicitud.BORRADOR.validarTransicionHacia(EstadoSolicitud.ANULADA));
    }

    @Test
    @DisplayName("R1: Transiciones válidas en flujo completo (EN_REVISION -> APROBADA -> PAGADA)")
    void flujoCompletoTransicionesValidas() {
        assertDoesNotThrow(() -> EstadoSolicitud.EN_REVISION.validarTransicionHacia(EstadoSolicitud.APROBADA));
        assertDoesNotThrow(() -> EstadoSolicitud.EN_REVISION.validarTransicionHacia(EstadoSolicitud.RECHAZADA));
        assertDoesNotThrow(() -> EstadoSolicitud.APROBADA.validarTransicionHacia(EstadoSolicitud.PAGADA));
        assertDoesNotThrow(() -> EstadoSolicitud.RECHAZADA.validarTransicionHacia(EstadoSolicitud.BORRADOR));
    }

    @Test
    @DisplayName("R1: Transacción inválida desde BORRADOR a PAGADA debe lanzar TransicionInvalidaException")
    void borradorNoPermitePagarDirectamente() {
        assertThrows(
            TransicionInvalidaException.class,
            () -> EstadoSolicitud.BORRADOR.validarTransicionHacia(EstadoSolicitud.PAGADA)
        );
    }

    @Test
    @DisplayName("R1: Estado PAGADA es inmutable y no permite transiciones")
    void pagadaNoPermiteTransiciones() {
        assertThrows(
            TransicionInvalidaException.class,
            () -> EstadoSolicitud.PAGADA.validarTransicionHacia(EstadoSolicitud.BORRADOR)
        );
    }
}

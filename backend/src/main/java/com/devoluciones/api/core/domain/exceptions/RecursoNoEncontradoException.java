package com.devoluciones.api.core.domain.exceptions;

/**
 * Excepción lanzada cuando un recurso solicitado no existe en el sistema (Mapea a HTTP 404).
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}

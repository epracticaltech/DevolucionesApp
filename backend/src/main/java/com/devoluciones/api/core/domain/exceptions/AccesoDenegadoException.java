package com.devoluciones.api.core.domain.exceptions;

/**
 * Excepción lanzada cuando el usuario no cuenta con el rol requerido para ejecutar una acción (Mapea a HTTP 403 Forbidden).
 */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}

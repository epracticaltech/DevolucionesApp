package com.devoluciones.api.core.domain.models.pagination;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Contenedor de Paginación agnóstico de frameworks para la capa de Dominio.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginaResultado<T> {

    private List<T> contenido;
    private int pagina;
    private int tamano;
    private long totalElementos;
    private int totalPaginas;
}

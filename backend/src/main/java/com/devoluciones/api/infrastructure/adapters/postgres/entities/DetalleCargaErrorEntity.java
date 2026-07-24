package com.devoluciones.api.infrastructure.adapters.postgres.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "detalles_carga_error")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleCargaErrorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "carga_id", nullable = false)
    private Long cargaId;

    @Column(name = "numero_fila", nullable = false)
    private int numeroFila;

    @Column(name = "campo", length = 100)
    private String campo;

    @Column(name = "motivo", nullable = false, columnDefinition = "TEXT")
    private String motivo;
}

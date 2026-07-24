package com.devoluciones.api.infrastructure.adapters.postgres.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cargas_masivas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CargaMasivaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_archivo", nullable = false)
    private String nombreArchivo;

    @Column(name = "total_filas", nullable = false)
    private int totalFilas;

    @Column(name = "filas_procesadas", nullable = false)
    private int filasProcesadas;

    @Column(name = "filas_rechazadas", nullable = false)
    private int filasRechazadas;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado;

    @Column(name = "fecha_carga", nullable = false)
    private LocalDateTime fechaCarga;
}

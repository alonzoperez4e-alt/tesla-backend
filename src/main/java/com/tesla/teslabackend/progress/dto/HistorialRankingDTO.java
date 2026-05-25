package com.tesla.teslabackend.progress.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class HistorialRankingDTO {
    private Integer idHistorial;
    private String nombreCompleto;
    private Integer expObtenida;
    private Integer posicion;
    private LocalDate fechaFinSemana;
}
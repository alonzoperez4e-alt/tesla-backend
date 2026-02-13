package com.tesla.teslabackend.dto.ranking;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RankingItemDTO {
    private Integer idUsuario;
    private String nombreCompleto;
    private String inicial;
    private Integer expTotal;
    private int posicionActual;
    private int tendencia;
    private boolean esUsuarioActual;
}

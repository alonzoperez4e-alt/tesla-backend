package com.tesla.teslabackend.dto;

public record LeccionDTO(
        Integer idLeccion,
        String nombre,
        Integer orden,
        boolean completada, // Para mostrar el check verde o estrella
        Integer puntajeObtenido // Opcional, por si queremos mostrar "30 pts"
) {}
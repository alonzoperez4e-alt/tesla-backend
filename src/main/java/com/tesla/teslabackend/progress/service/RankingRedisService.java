package com.tesla.teslabackend.progress.service;

import com.tesla.teslabackend.common.util.TimeUtil;
import com.tesla.teslabackend.progress.repository.IntentoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RankingRedisService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private IntentoRepository intentoRepository;

    public void registrarExpSemanal(Integer idUsuario, int expGanada, ZonedDateTime fecha) {
        try {
            String clave = "ranking:sem:" + TimeUtil.semanaISOdeLima(fecha);

            redisTemplate.opsForZSet().incrementScore(clave, String.valueOf(idUsuario), expGanada);

            redisTemplate.expire(clave, 14, TimeUnit.DAYS);

            log.debug("Redis actualizado: {} exp añadida al usuario {} en {}", expGanada, idUsuario, clave);
        } catch (Exception e) {
            log.warn("Redis no actualizado, se reconstruirá en lectura para el usuario {}: {}", idUsuario, e.getMessage());
        }
    }

    public void reconstruirDesdeAurora(String clave, ZonedDateTime inicio, ZonedDateTime fin) {
        log.info("Reconstruyendo ranking en Redis para la clave: {}", clave);
        List<Object[]> agregados = intentoRepository.findExpAgregadaPorVentana(inicio, fin);

        if (agregados.isEmpty()) return;

        for (Object[] row : agregados) {
            String idUsuarioStr = String.valueOf((Integer) row[0]);
            Long expTotal = (Long) row[1];
            redisTemplate.opsForZSet().add(clave, idUsuarioStr, expTotal.doubleValue());
        }

        redisTemplate.expire(clave, 14, TimeUnit.DAYS);
    }
}
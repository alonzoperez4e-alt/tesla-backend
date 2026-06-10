package com.tesla.teslabackend.progress.service;

import com.tesla.teslabackend.common.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RankingRedisService {

    @Autowired
    private StringRedisTemplate redisTemplate;

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
}
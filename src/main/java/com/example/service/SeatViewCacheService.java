package com.example.service;

import com.example.entity.Seat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Duration;
import java.util.List;
import java.util.Set;

@Service
public class SeatViewCacheService {

    private static final Logger log = LoggerFactory.getLogger(SeatViewCacheService.class);

    private static final String KEY_PREFIX = "seat:view:v1";
    private static final String USER_ANON = "anon";

    private static final TypeReference<List<Seat>> SEAT_LIST_TYPE = new TypeReference<>() {};

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration cacheTtl;
    private final Duration indexTtl;
    private final int invalidateRangeDays;

    public SeatViewCacheService(
            StringRedisTemplate redisTemplate,
            @Value("${seat.view.cache.ttl-seconds:60}") long ttlSeconds,
            @Value("${seat.view.cache.invalidate-range-days:31}") int invalidateRangeDays
    ) {
        this.redisTemplate = redisTemplate;
        this.cacheTtl = Duration.ofSeconds(Math.max(5, ttlSeconds));
        this.indexTtl = this.cacheTtl.multipliedBy(2);
        this.invalidateRangeDays = Math.max(1, invalidateRangeDays);
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public List<Seat> get(LocalDate date, String userId, String buildingCode, Integer floorNo) {
        String viewKey = viewKey(date, userId, buildingCode, floorNo);
        try {
            String payload = redisTemplate.opsForValue().get(viewKey);
            if (payload == null || payload.isBlank()) {
                return null;
            }
            return objectMapper.readValue(payload, SEAT_LIST_TYPE);
        } catch (Exception e) {
            log.warn("seat view cache read failed, key={}", viewKey, e);
            return null;
        }
    }

    public void put(LocalDate date, String userId, String buildingCode, Integer floorNo, List<Seat> seats) {
        if (seats == null) {
            return;
        }
        String viewKey = viewKey(date, userId, buildingCode, floorNo);
        String indexKey = indexKey(date, buildingCode, floorNo);
        try {
            String payload = objectMapper.writeValueAsString(seats);
            redisTemplate.opsForValue().set(viewKey, payload, cacheTtl);
            redisTemplate.opsForSet().add(indexKey, viewKey);
            redisTemplate.expire(indexKey, indexTtl);
        } catch (JsonProcessingException e) {
            log.warn("seat view cache write skipped due to json error, key={}", viewKey, e);
        } catch (Exception e) {
            log.warn("seat view cache write failed, key={}", viewKey, e);
        }
    }

    public void invalidateDate(LocalDate date) {
        String indexPattern = indexKey(date, "*", null);
        try {
            Set<String> indexKeys = redisTemplate.keys(indexPattern);
            if (indexKeys == null || indexKeys.isEmpty()) {
                return;
            }

            for (String indexKey : indexKeys) {
                Set<String> keys = redisTemplate.opsForSet().members(indexKey);
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                }
                redisTemplate.delete(indexKey);
            }
        } catch (Exception e) {
            log.warn("seat view cache invalidate failed, date={}", date, e);
        }
    }

    public void invalidateDateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return;
        }

        if (end.isBefore(start)) {
            LocalDate t = start;
            start = end;
            end = t;
        }

        LocalDate cursor = start;
        int guard = 0;
        while (!cursor.isAfter(end) && guard < invalidateRangeDays) {
            invalidateDate(cursor);
            cursor = cursor.plusDays(1);
            guard++;
        }
    }

    private String viewKey(LocalDate date, String userId, String buildingCode, Integer floorNo) {
        String safeUser = (userId == null || userId.isBlank()) ? USER_ANON : userId.trim();
        String safeBuilding = (buildingCode == null || buildingCode.isBlank()) ? "B1" : buildingCode.trim().toUpperCase();
        int safeFloor = floorNo == null ? 1 : floorNo;
        return KEY_PREFIX + ":date:" + date + ":building:" + safeBuilding + ":floor:" + safeFloor + ":user:" + safeUser;
    }

    private String indexKey(LocalDate date, String buildingCode, Integer floorNo) {
        String safeBuilding = (buildingCode == null || buildingCode.isBlank()) ? "*" : buildingCode.trim().toUpperCase();
        String safeFloor = floorNo == null ? "*" : String.valueOf(floorNo);
        return KEY_PREFIX + ":index:date:" + date + ":building:" + safeBuilding + ":floor:" + safeFloor;
    }
}

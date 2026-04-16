package ru.practicum.ewm.stats.mapper;

import ru.practicum.ewm.stats.dto.InputHitDto;
import ru.practicum.ewm.stats.model.Hit;

public class HitMapper {
    public static Hit toHit(InputHitDto hitDto) {
        Hit hit = new Hit();
        hit.setApp(hitDto.getApp());
        hit.setIp(hitDto.getIp());
        hit.setUri(hitDto.getUri());
        hit.setCreated(hitDto.getTimestamp());
        return hit;
    }
}

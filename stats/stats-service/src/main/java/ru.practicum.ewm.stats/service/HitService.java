package ru.practicum.ewm.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.dto.InputHitDto;
import ru.practicum.ewm.stats.dto.ResponseHitDto;
import ru.practicum.ewm.stats.mapper.HitMapper;
import ru.practicum.ewm.stats.repository.HitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HitService {
    private final HitRepository repository;

    public void save(InputHitDto hitDto) {
        repository.save(HitMapper.toHit(hitDto));
    }

    public List<ResponseHitDto> getStats(LocalDateTime start, LocalDateTime end,
                                         List<String> uris, boolean unique) {
        boolean hasUris = uris != null && !uris.isEmpty();

        if (!hasUris && !unique) {
            return repository.findStats(start, end);
        } else if (!hasUris) {
            return repository.findStatsWithUniqueIp(start, end);
        } else if (!unique) {
            return repository.findStatsForUris(start, end, uris);
        } else {
            return repository.findStatsForUrisWithUniqueIp(start, end, uris);
        }
    }
}

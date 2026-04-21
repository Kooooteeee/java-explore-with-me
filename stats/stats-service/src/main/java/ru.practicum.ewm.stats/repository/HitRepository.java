package ru.practicum.ewm.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.stats.dto.ResponseHitDto;
import ru.practicum.ewm.stats.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

public interface HitRepository extends JpaRepository<Hit, Long> {

    @Query("select new ru.practicum.ewm.stats.dto.ResponseHitDto(h.app, h.uri, count(h)) " +
            "from Hit h " +
            "where h.created >= :start and h.created <= :end " +
            "group by h.app, h.uri")
    List<ResponseHitDto> findStats(@Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end);


    @Query("select new ru.practicum.ewm.stats.dto.ResponseHitDto(h.app, h.uri, count(h)) " +
            "from Hit h " +
            "where h.created >= :start and h.created <= :end and h.uri in (:uris) " +
            "group by h.app, h.uri")
    List<ResponseHitDto> findStatsForUris(@Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end,
                                          @Param("uris") List<String> uris);

    @Query("select new ru.practicum.ewm.stats.dto.ResponseHitDto(h.app, h.uri, count(distinct h.ip)) " +
            "from Hit h " +
            "where h.created >= :start and h.created <= :end " +
            "group by h.app, h.uri")
    List<ResponseHitDto> findStatsWithUniqueIp(@Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end);

    @Query("select new ru.practicum.ewm.stats.dto.ResponseHitDto(h.app, h.uri, count(distinct h.ip)) " +
            "from Hit h " +
            "where h.created >= :start and h.created <= :end and h.uri in (:uris) " +
            "group by h.app, h.uri")
    List<ResponseHitDto> findStatsForUrisWithUniqueIp(@Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end,
                                          @Param("uris") List<String> uris);
}

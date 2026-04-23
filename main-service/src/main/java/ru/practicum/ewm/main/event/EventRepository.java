package ru.practicum.ewm.main.event;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.main.request.RequestStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAllByInitiatorId(Long initiatorId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long initiatorId);

    List<Event> findAllByIdIn(List<Long> ids);

    @Query("select e from Event e " +
            "where (:users is null or e.initiator.id in :users) " +
            "and (:states is null or e.state in :states) " +
            "and (:categories is null or e.category.id in :categories) " +
            "and (:rangeStart is null or e.eventDate >= :rangeStart) " +
            "and (:rangeEnd is null or e.eventDate <= :rangeEnd)")
    List<Event> findAdminEvents(@Param("users") List<Long> users,
                                @Param("states") List<EventState> states,
                                @Param("categories") List<Long> categories,
                                @Param("rangeStart") LocalDateTime rangeStart,
                                @Param("rangeEnd") LocalDateTime rangeEnd,
                                Pageable pageable);

    @Query("select e from Event e " +
            "where e.state = ru.practicum.ewm.main.event.EventState.PUBLISHED " +
            "and (:text is null or " +
            "lower(e.annotation) like lower(concat('%', :text, '%')) or " +
            "lower(e.description) like lower(concat('%', :text, '%'))) " +
            "and (:categories is null or e.category.id in :categories) " +
            "and (:paid is null or e.paid = :paid) " +
            "and (:rangeStart is null or e.eventDate >= :rangeStart) " +
            "and (:rangeEnd is null or e.eventDate <= :rangeEnd) " +
            "and (:onlyAvailable = false or e.participantLimit = 0 or " +
            "e.participantLimit > (" +
            "select count(r) from EventRequest r " +
            "where r.event.id = e.id " +
            "and r.status = ru.practicum.ewm.main.request.RequestStatus.CONFIRMED))")
    List<Event> findPublicEvents(@Param("text") String text,
                                 @Param("categories") List<Long> categories,
                                 @Param("paid") Boolean paid,
                                 @Param("rangeStart") LocalDateTime rangeStart,
                                 @Param("rangeEnd") LocalDateTime rangeEnd,
                                 @Param("onlyAvailable") boolean onlyAvailable,
                                 Pageable pageable);

    @Query("select e from Event e " +
            "where e.state = ru.practicum.ewm.main.event.EventState.PUBLISHED " +
            "and (:text is null or " +
            "lower(e.annotation) like lower(concat('%', :text, '%')) or " +
            "lower(e.description) like lower(concat('%', :text, '%'))) " +
            "and (:categories is null or e.category.id in :categories) " +
            "and (:paid is null or e.paid = :paid) " +
            "and (:rangeStart is null or e.eventDate >= :rangeStart) " +
            "and (:rangeEnd is null or e.eventDate <= :rangeEnd) " +
            "and (:onlyAvailable = false or e.participantLimit = 0 or " +
            "e.participantLimit > (" +
            "select count(r) from EventRequest r " +
            "where r.event.id = e.id " +
            "and r.status = ru.practicum.ewm.main.request.RequestStatus.CONFIRMED))")
    List<Event> findPublicEvents(@Param("text") String text,
                                 @Param("categories") List<Long> categories,
                                 @Param("paid") Boolean paid,
                                 @Param("rangeStart") LocalDateTime rangeStart,
                                 @Param("rangeEnd") LocalDateTime rangeEnd,
                                 @Param("onlyAvailable") boolean onlyAvailable);
}
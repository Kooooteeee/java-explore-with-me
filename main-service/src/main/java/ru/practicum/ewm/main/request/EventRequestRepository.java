package ru.practicum.ewm.main.request;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRequestRepository extends JpaRepository<EventRequest, Long> {

    List<EventRequest> findAllByRequesterId(Long requesterId);

    List<EventRequest> findAllByEventId(Long eventId);

    boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);

    List<EventRequest> findAllByEventIdAndIdIn(Long eventId, List<Long> ids);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);
}

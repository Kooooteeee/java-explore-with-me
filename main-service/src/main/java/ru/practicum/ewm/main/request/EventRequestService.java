package ru.practicum.ewm.main.request;

import java.util.List;

public interface EventRequestService {

    EventRequestDto createRequest(Long userId, Long eventId);

    List<EventRequestDto> findAllUserRequests(Long userId);

    EventRequestDto cancelRequest(Long userId, Long requestId);

    List<EventRequestDto> findAllEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResultDto updateRequestStatus(Long userId,
                                                          Long eventId,
                                                          EventRequestStatusUpdateDto updateDto);
}
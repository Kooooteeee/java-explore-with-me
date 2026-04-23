package ru.practicum.ewm.main.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}")
public class EventRequestController {

    private final EventRequestService eventRequestService;

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public EventRequestDto createRequest(@PathVariable Long userId,
                                         @RequestParam Long eventId) {
        return eventRequestService.createRequest(userId, eventId);
    }

    @GetMapping("/requests")
    public List<EventRequestDto> findAllUserRequests(@PathVariable Long userId) {
        return eventRequestService.findAllUserRequests(userId);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public EventRequestDto cancelRequest(@PathVariable Long userId,
                                         @PathVariable Long requestId) {
        return eventRequestService.cancelRequest(userId, requestId);
    }

    @GetMapping("/events/{eventId}/requests")
    public List<EventRequestDto> findAllEventRequests(@PathVariable Long userId,
                                                      @PathVariable Long eventId) {
        return eventRequestService.findAllEventRequests(userId, eventId);
    }

    @PatchMapping("/events/{eventId}/requests")
    public EventRequestStatusUpdateResultDto updateRequestStatus(@PathVariable Long userId,
                                                                 @PathVariable Long eventId,
                                                                 @RequestBody EventRequestStatusUpdateDto updateDto) {
        return eventRequestService.updateRequestStatus(userId, eventId, updateDto);
    }
}
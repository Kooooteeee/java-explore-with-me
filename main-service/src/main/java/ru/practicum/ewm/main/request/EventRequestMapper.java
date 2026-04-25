package ru.practicum.ewm.main.request;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventRequestMapper {

    public static EventRequestDto toEventRequestDto(EventRequest eventRequest) {
        EventRequestDto eventRequestDto = new EventRequestDto();
        eventRequestDto.setId(eventRequest.getId());
        eventRequestDto.setRequester(eventRequest.getRequester().getId());
        eventRequestDto.setEvent(eventRequest.getEvent().getId());
        eventRequestDto.setStatus(eventRequest.getStatus());
        eventRequestDto.setCreated(eventRequest.getCreated());
        return eventRequestDto;
    }
}
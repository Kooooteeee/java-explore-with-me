package ru.practicum.ewm.main.request;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class EventRequestStatusUpdateResultDto {

    private List<EventRequestDto> confirmedRequests = new ArrayList<>();
    private List<EventRequestDto> rejectedRequests = new ArrayList<>();
}
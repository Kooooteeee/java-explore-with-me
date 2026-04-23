package ru.practicum.ewm.main.request;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class EventRequestStatusUpdateDto {

    private List<Long> requestIds = new ArrayList<>();
    private RequestStatus status;
}
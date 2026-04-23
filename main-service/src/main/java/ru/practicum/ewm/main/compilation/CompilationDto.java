package ru.practicum.ewm.main.compilation;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.ewm.main.event.EventShortDto;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CompilationDto {

    private Long id;
    private String title;
    private Boolean pinned;
    private List<EventShortDto> events = new ArrayList<>();
}
package ru.practicum.ewm.main.compilation;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.Set;

@Getter
@Setter
public class UpdateCompilationDto {

    private Collection<Long> events;

    private Boolean pinned;

    @Size(max = 50)
    private String title;
}
package ru.practicum.ewm.main.compilation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.main.event.Event;
import ru.practicum.ewm.main.event.EventMapper;
import ru.practicum.ewm.main.event.EventRepository;
import ru.practicum.ewm.main.event.EventShortDto;
import ru.practicum.ewm.main.exception.NotFoundException;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = CompilationMapper.toCompilation(newCompilationDto);
        compilation.setEvents(findAllEventsByIds(newCompilationDto.getEvents()));
        Compilation savedCompilation = compilationRepository.save(compilation);
        return toCompilationDto(savedCompilation);
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationDto updateCompilationDto) {
        Compilation compilation = findByIdOrThrow(compId);

        if (updateCompilationDto.getTitle() != null) {
            compilation.setTitle(updateCompilationDto.getTitle());
        }

        if (updateCompilationDto.getPinned() != null) {
            compilation.setPinned(updateCompilationDto.getPinned());
        }

        if (updateCompilationDto.getEvents() != null) {
            compilation.setEvents(findAllEventsByIds(updateCompilationDto.getEvents()));
        }

        Compilation updatedCompilation = compilationRepository.save(compilation);
        return toCompilationDto(updatedCompilation);
    }

    @Override
    public void deleteCompilationById(Long compId) {
        findByIdOrThrow(compId);
        compilationRepository.deleteById(compId);
    }

    @Override
    public CompilationDto findCompilationById(Long compId) {
        return toCompilationDto(findByIdOrThrow(compId));
    }

    @Override
    public List<CompilationDto> findAll(Boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        List<Compilation> compilations;
        if (pinned == null) {
            compilations = compilationRepository.findAll(pageable).getContent();
        } else {
            compilations = compilationRepository.findAllByPinned(pinned, pageable);
        }

        return compilations.stream()
                .map(this::toCompilationDto)
                .toList();
    }

    private Compilation findByIdOrThrow(Long id) {
        Optional<Compilation> compilation = compilationRepository.findById(id);
        return compilation.orElseThrow(() -> new NotFoundException("Такой подборки нет!"));
    }

    private Set<Event> findAllEventsByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(eventRepository.findAllByIdIn(new ArrayList<>(ids)));
    }

    private CompilationDto toCompilationDto(Compilation compilation) {
        List<EventShortDto> eventShortDtos = compilation.getEvents().stream()
                .map(event -> {
                    return EventMapper.toEventShortDto(event, 0L, 0L);
                })
                .toList();

        return CompilationMapper.toCompilationDto(compilation, eventShortDtos);
    }
}

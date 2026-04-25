package ru.practicum.ewm.main.compilation;

import java.util.List;

public interface CompilationService {
    CompilationDto createCompilation(NewCompilationDto newCompilationDto);

    CompilationDto updateCompilation(Long compId, UpdateCompilationDto updateCompilationDto);

    void deleteCompilationById(Long compId);

    CompilationDto findCompilationById(Long compId);

    List<CompilationDto> findAll(Boolean pinned, int from, int size);


}

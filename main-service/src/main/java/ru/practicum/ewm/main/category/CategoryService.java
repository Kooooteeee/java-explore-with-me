package ru.practicum.ewm.main.category;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(NewCategoryDto newCategoryDto);

    CategoryDto updateCategory(Long id, CategoryDto categoryDto);

    void deleteCategoryById(Long id);

    List<CategoryDto> findAll(int from, int size);

    CategoryDto findCategoryById(Long id);
}

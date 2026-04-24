package ru.practicum.ewm.main.category;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.main.exception.ConflictException;
import ru.practicum.ewm.main.exception.NotFoundException;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        if (!isUniqueName(newCategoryDto.getName())) {
            throw new ConflictException("Такая категория уже существует!");
        }
        Category category = categoryRepository.save(CategoryMapper.toCategory(newCategoryDto));
        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        if (categoryRepository.existsByNameAndIdNot(categoryDto.getName(), id)) {
            throw new ConflictException("Категория с таким именем уже существует!");
        }
        Category category = findByIdOrThrow(id);
        category.setName(categoryDto.getName());
        Category updatedCategory = categoryRepository.save(category);
        return CategoryMapper.toCategoryDto(updatedCategory);
    }

    @Override
    public void deleteCategoryById(Long id) {
        findByIdOrThrow(id);
        categoryRepository.deleteById(id);
    }

    @Override
    public List<CategoryDto> findAll(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Category> categories = categoryRepository.findAll(pageable).getContent();
        return categories.stream()
                .map(CategoryMapper::toCategoryDto)
                .toList();
    }

    @Override
    public CategoryDto findCategoryById(Long id) {
        return CategoryMapper.toCategoryDto(findByIdOrThrow(id));
    }

    private boolean isUniqueName(String name) {
        return !categoryRepository.existsByName(name);
    }

    private Category findByIdOrThrow(Long id) {
        Optional<Category> category = categoryRepository.findById(id);
        return category.orElseThrow(() -> new NotFoundException("Такой категории нет!"));
    }
}

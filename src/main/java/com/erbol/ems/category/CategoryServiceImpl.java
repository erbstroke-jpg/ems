package com.erbol.ems.category;

import com.erbol.ems.category.dto.CategoryCreateDto;
import com.erbol.ems.category.dto.CategoryUpdateDto;
import com.erbol.ems.common.exception.DuplicateResourceException;
import com.erbol.ems.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private final CategoryRepository repository;

    public CategoryServiceImpl(CategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Category> findAll() {
        return repository.findAllByOrderByNameAsc();
    }

    @Override
    public Category findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }

    @Override
    @Transactional
    public Category create(CategoryCreateDto dto) {
        String normalizedName = dto.getName().trim();

        if (repository.existsByName(normalizedName)) {
            throw new DuplicateResourceException(
                    "Category with name '" + normalizedName + "' already exists");
        }

        Category category = new Category(
                normalizedName,
                dto.getDescription() != null ? dto.getDescription().trim() : null
        );
        Category saved = repository.save(category);
        log.info("Created category: id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }

    @Override
    @Transactional
    public Category update(Long id, CategoryUpdateDto dto) {
        Category category = findById(id);
        String normalizedName = dto.getName().trim();

        if (repository.existsByNameAndIdNot(normalizedName, id)) {
            throw new DuplicateResourceException(
                    "Another category with name '" + normalizedName + "' already exists");
        }

        category.setName(normalizedName);
        category.setDescription(
                dto.getDescription() != null ? dto.getDescription().trim() : null
        );
        // No need to call save() — Hibernate will detect the dirty state
        // and flush at the end of the transaction (automatic dirty checking).
        log.info("Updated category: id={}, name={}", category.getId(), category.getName());
        return category;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category category = findById(id);
        repository.delete(category);
        log.info("Deleted category: id={}, name={}", id, category.getName());
    }
}
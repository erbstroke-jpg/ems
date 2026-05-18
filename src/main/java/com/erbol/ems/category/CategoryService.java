package com.erbol.ems.category;

import com.erbol.ems.category.dto.CategoryCreateDto;
import com.erbol.ems.category.dto.CategoryUpdateDto;

import java.util.List;

/**
 * Service abstraction for category management.
 *
 * <p>Categories classify events (Technology, Music, Business, etc).
 * Only administrators can mutate categories; all roles can read them.
 */
public interface CategoryService {

    /**
     * @return all categories, ordered alphabetically by name.
     */
    List<Category> findAll();

    /**
     * @throws com.erbol.ems.common.exception.ResourceNotFoundException
     *         if no category exists with the given id.
     */
    Category findById(Long id);

    /**
     * @throws com.erbol.ems.common.exception.DuplicateResourceException
     *         if a category with the same name already exists.
     */
    Category create(CategoryCreateDto dto);

    /**
     * @throws com.erbol.ems.common.exception.ResourceNotFoundException
     *         if the target category does not exist.
     * @throws com.erbol.ems.common.exception.DuplicateResourceException
     *         if another category already uses the new name.
     */
    Category update(Long id, CategoryUpdateDto dto);

    /**
     * @throws com.erbol.ems.common.exception.ResourceNotFoundException
     *         if the target category does not exist.
     */
    void delete(Long id);
}
package com.erbol.ems.category;

import com.erbol.ems.category.dto.CategoryCreateDto;
import com.erbol.ems.category.dto.CategoryUpdateDto;
import com.erbol.ems.common.exception.DuplicateResourceException;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
public class CategoryAdminController {

    private final CategoryService categoryService;

    public CategoryAdminController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "admin/categories/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("categoryCreateDto", new CategoryCreateDto());
        return "admin/categories/form-create";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute CategoryCreateDto dto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/categories/form-create";
        }
        try {
            Category created = categoryService.create(dto);
            redirectAttributes.addFlashAttribute(
                    "successMessage", "Category '" + created.getName() + "' created.");
        } catch (DuplicateResourceException ex) {
            bindingResult.rejectValue("name", "name.duplicate", ex.getMessage());
            return "admin/categories/form-create";
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Category category = categoryService.findById(id);
        CategoryUpdateDto dto = CategoryUpdateDto.builder()
                .name(category.getName())
                .description(category.getDescription())
                .build();
        model.addAttribute("categoryUpdateDto", dto);
        model.addAttribute("categoryId", id);
        return "admin/categories/form-edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute CategoryUpdateDto dto,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categoryId", id);
            return "admin/categories/form-edit";
        }
        try {
            Category updated = categoryService.update(id, dto);
            redirectAttributes.addFlashAttribute(
                    "successMessage", "Category '" + updated.getName() + "' updated.");
        } catch (DuplicateResourceException ex) {
            bindingResult.rejectValue("name", "name.duplicate", ex.getMessage());
            model.addAttribute("categoryId", id);
            return "admin/categories/form-edit";
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         RedirectAttributes redirectAttributes) {
        Category category = categoryService.findById(id);
        categoryService.delete(id);
        redirectAttributes.addFlashAttribute(
                "successMessage", "Category '" + category.getName() + "' deleted.");
        return "redirect:/admin/categories";
    }
}
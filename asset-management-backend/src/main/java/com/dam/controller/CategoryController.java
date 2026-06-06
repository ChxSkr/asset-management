package com.dam.controller;

import com.dam.common.Result;
import com.dam.entity.Category;
import com.dam.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/tree")
    public Result<?> buildTree() {
        return categoryService.buildTree();
    }

    @PostMapping
    public Result<?> add(HttpServletRequest request, @RequestBody Category category) {
        return categoryService.add(category);
    }

    @PutMapping("/{id}")
    public Result<?> update(HttpServletRequest request, @PathVariable Long id, @RequestBody Category category) {
        return categoryService.update(id, category);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(HttpServletRequest request, @PathVariable Long id) {
        return categoryService.delete(id);
    }
}
package com.dam.service;

import com.dam.common.Result;
import com.dam.entity.Category;

public interface CategoryService {
    Result<?> buildTree();
    Result<?> add(Category category);
    Result<?> update(Long id, Category category);
    Result<?> delete(Long id);
}
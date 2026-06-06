package com.dam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dam.common.Result;
import com.dam.entity.Category;
import com.dam.mapper.CategoryMapper;
import com.dam.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public Result<?> buildTree() {
        var allCategories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSortOrder));
        var tree = buildTree(allCategories, null);
        return Result.ok(tree);
    }

    private List<Map<String, Object>> buildTree(List<Category> allCategories, Long parentId) {
        var children = new ArrayList<Map<String, Object>>();
        for (var category : allCategories) {
            var catParentId = category.getParentId();
            if ((parentId == null && (catParentId == null || catParentId == 0L))
                    || (parentId != null && parentId.equals(catParentId))) {
                var node = new HashMap<String, Object>();
                node.put("categoryId", category.getCategoryId());
                node.put("categoryName", category.getCategoryName());
                node.put("parentId", category.getParentId());
                node.put("sortOrder", category.getSortOrder());
                node.put("createdAt", category.getCreatedAt());
                var subChildren = buildTree(allCategories, category.getCategoryId());
                node.put("children", subChildren);
                children.add(node);
            }
        }
        return children;
    }

    @Override
    public Result<?> add(Category category) {
        category.setCategoryId(null);
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        categoryMapper.insert(category);
        return Result.ok("添加成功", category);
    }

    @Override
    public Result<?> update(Long id, Category category) {
        var existCategory = categoryMapper.selectById(id);
        if (existCategory == null) {
            return Result.error("分类不存在");
        }
        category.setCategoryId(id);
        categoryMapper.updateById(category);
        return Result.ok("更新成功");
    }

    @Override
    public Result<?> delete(Long id) {
        var existCategory = categoryMapper.selectById(id);
        if (existCategory == null) {
            return Result.error("分类不存在");
        }
        var childCount = categoryMapper.selectCount(
                new LambdaQueryWrapper<Category>().eq(Category::getParentId, id));
        if (childCount > 0) {
            return Result.error("该分类下存在子分类，无法删除");
        }
        categoryMapper.deleteById(id);
        return Result.ok("删除成功");
    }
}
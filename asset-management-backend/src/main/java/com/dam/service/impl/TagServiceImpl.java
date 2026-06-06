package com.dam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dam.common.Result;
import com.dam.entity.AssetTag;
import com.dam.entity.Tag;
import com.dam.mapper.AssetTagMapper;
import com.dam.mapper.TagMapper;
import com.dam.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;
    private final AssetTagMapper assetTagMapper;

    @Override
    public Result<?> getList() {
        var tags = tagMapper.selectList(
                new LambdaQueryWrapper<Tag>().orderByDesc(Tag::getCreatedAt));

        var list = tags.stream().map(tag -> {
            var count = assetTagMapper.selectCount(
                    new LambdaQueryWrapper<AssetTag>().eq(AssetTag::getTagId, tag.getTagId()));
            Map<String, Object> item = new HashMap<>();
            item.put("tagId", tag.getTagId());
            item.put("tagName", tag.getTagName());
            item.put("assetCount", count);
            item.put("createdAt", tag.getCreatedAt());
            return (Object) item;
        }).toList();

        return Result.ok(list);
    }

    @Override
    public Result<?> add(String tagName) {
        if (tagName == null || tagName.isBlank()) {
            return Result.error("标签名不能为空");
        }
        var existTag = tagMapper.selectOne(
                new LambdaQueryWrapper<Tag>().eq(Tag::getTagName, tagName));
        if (existTag != null) {
            return Result.error("标签已存在");
        }
        var tag = new Tag();
        tag.setTagName(tagName);
        tagMapper.insert(tag);
        return Result.ok("添加成功", tag);
    }

    @Override
    public Result<?> delete(Long id) {
        var tag = tagMapper.selectById(id);
        if (tag == null) {
            return Result.error("标签不存在");
        }
        tagMapper.deleteById(id);
        return Result.ok("删除成功");
    }
}
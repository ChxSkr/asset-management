package com.dam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dam.common.Result;
import com.dam.dto.SearchDTO;
import com.dam.entity.Asset;
import com.dam.entity.AssetTag;
import com.dam.mapper.AssetMapper;
import com.dam.mapper.AssetTagMapper;
import com.dam.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final AssetMapper assetMapper;
    private final AssetTagMapper assetTagMapper;

    @Override
    public Result<?> search(SearchDTO dto) {
        var wrapper = new LambdaQueryWrapper<Asset>();

        if (dto.getKeyword() != null && !dto.getKeyword().isBlank()) {
            wrapper.and(w -> w
                    .like(Asset::getAssetName, dto.getKeyword())
                    .or()
                    .like(Asset::getDescription, dto.getKeyword()));
        }
        if (dto.getCategoryId() != null) {
            wrapper.eq(Asset::getCategoryId, dto.getCategoryId());
        }
        if (dto.getFileType() != null && !dto.getFileType().isBlank()) {
            wrapper.eq(Asset::getFileType, dto.getFileType());
        }
        if (dto.getStartTime() != null && !dto.getStartTime().isBlank()) {
            wrapper.ge(Asset::getCreatedAt, dto.getStartTime());
        }
        if (dto.getEndTime() != null && !dto.getEndTime().isBlank()) {
            wrapper.le(Asset::getCreatedAt, dto.getEndTime());
        }

        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            var assetIds = assetTagMapper.selectList(
                            new LambdaQueryWrapper<AssetTag>().in(AssetTag::getTagId, dto.getTagIds()))
                    .stream()
                    .map(AssetTag::getAssetId)
                    .distinct()
                    .toList();
            if (assetIds.isEmpty()) {
                return Result.ok(Page.of(dto.getPage(), dto.getPageSize()));
            }
            wrapper.in(Asset::getAssetId, assetIds);
        }

        wrapper.orderByDesc(Asset::getCreatedAt);

        var page = assetMapper.selectPage(
                new Page<>(dto.getPage(), dto.getPageSize()), wrapper);
        return Result.ok(page);
    }
}
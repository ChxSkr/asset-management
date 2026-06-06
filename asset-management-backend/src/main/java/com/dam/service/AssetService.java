package com.dam.service;

import com.dam.common.Result;
import com.dam.dto.SearchDTO;
import com.dam.dto.UpdateAssetDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AssetService {
    Result<?> upload(MultipartFile file, String name, Long categoryId, List<Long> tagIds, String description, Long userId, String ipAddress);
    Result<?> getList(SearchDTO dto);
    Result<?> getDetail(Long id);
    Result<?> update(Long id, UpdateAssetDTO dto, Long userId, String ipAddress);
    Result<?> softDelete(Long id, Long userId, String ipAddress);
    Result<?> restore(Long id, Long userId, String ipAddress);
    Result<?> permanentDelete(Long id, Long userId, String ipAddress);
    Result<?> download(Long id, Long userId, String ipAddress);
    Result<?> getRecycleList(int page, int pageSize);
}
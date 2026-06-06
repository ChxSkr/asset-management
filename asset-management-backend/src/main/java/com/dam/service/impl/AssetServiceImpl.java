package com.dam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dam.common.Result;
import com.dam.dto.SearchDTO;
import com.dam.dto.UpdateAssetDTO;
import com.dam.entity.*;
import com.dam.mapper.*;
import com.dam.service.AssetService;
import com.dam.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    @Value("${file.upload-path}")
    private String uploadPath;

    private final AssetMapper assetMapper;
    private final AssetTagMapper assetTagMapper;
    private final AssetVersionMapper assetVersionMapper;
    private final TagMapper tagMapper;
    private final DownloadLogMapper downloadLogMapper;
    private final OperationLogMapper operationLogMapper;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public Result<?> upload(MultipartFile file, String name, Long categoryId, List<Long> tagIds, String description, Long userId, String ipAddress) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        var originalFilename = file.getOriginalFilename();
        if (!FileUtil.isAllowedExtension(originalFilename)) {
            return Result.error("不支持的文件格式");
        }
        if (!FileUtil.isSizeValid(file.getSize())) {
            return Result.error("文件大小超出限制");
        }

        var storageFilename = FileUtil.generateStorageFilename(originalFilename);
        try {
            FileUtil.saveFile(file.getBytes(), uploadPath, storageFilename);
        } catch (IOException e) {
            return Result.error("文件保存失败");
        }

        var asset = new Asset();
        var assetName = name != null ? name : originalFilename;
        asset.setAssetName(assetName);
        asset.setDescription(description);
        asset.setFileType(FileUtil.getExtension(originalFilename).replace(".", ""));
        asset.setFileSize(file.getSize());
        asset.setStoragePath(storageFilename);
        asset.setUploadUserId(userId);
        asset.setCategoryId(categoryId);
        asset.setCurrentVersion(1);
        asset.setIsDeleted(0);
        assetMapper.insert(asset);

        if (tagIds != null && !tagIds.isEmpty()) {
            for (var tagId : tagIds) {
                var assetTag = new AssetTag();
                assetTag.setAssetId(asset.getAssetId());
                assetTag.setTagId(tagId);
                assetTagMapper.insert(assetTag);
            }
        }

        var version = new AssetVersion();
        version.setAssetId(asset.getAssetId());
        version.setVersionNumber(1);
        version.setFileSize(file.getSize());
        version.setStoragePath(storageFilename);
        version.setUploadUserId(userId);
        version.setVersionNote("初始版本");
        assetVersionMapper.insert(version);

        var log = new OperationLog();
        log.setUserId(userId);
        log.setOperationType("UPLOAD");
        log.setDescription("上传资产: " + assetName);
        log.setIpAddress(ipAddress);
        operationLogMapper.insert(log);

        return Result.ok("上传成功", asset);
    }

    @Override
    public Result<?> getList(SearchDTO dto) {
        var wrapper = new LambdaQueryWrapper<Asset>();
        wrapper.eq(Asset::getIsDeleted, 0);

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
            var ft = dto.getFileType();
            if ("PDF".equalsIgnoreCase(ft)) {
                wrapper.apply("LOWER(file_type) = 'pdf'");
            } else if ("DOC".equalsIgnoreCase(ft)) {
                wrapper.in(Asset::getFileType, List.of("doc", "docx"));
            } else if ("IMAGE".equalsIgnoreCase(ft)) {
                wrapper.in(Asset::getFileType, List.of("jpg", "jpeg", "png", "gif", "svg", "webp", "bmp"));
            } else if ("VIDEO".equalsIgnoreCase(ft)) {
                wrapper.in(Asset::getFileType, List.of("mp4", "avi", "mov", "mkv", "wmv", "flv"));
            } else if ("AUDIO".equalsIgnoreCase(ft)) {
                wrapper.in(Asset::getFileType, List.of("mp3", "wav", "flac", "aac", "ogg"));
            } else if ("ZIP".equalsIgnoreCase(ft)) {
                wrapper.in(Asset::getFileType, List.of("zip", "rar", "7z", "gz", "tar"));
            } else if ("OTHER".equalsIgnoreCase(ft)) {
                wrapper.notIn(Asset::getFileType, List.of(
                    "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt",
                    "jpg", "jpeg", "png", "gif", "svg", "webp", "bmp",
                    "mp4", "avi", "mov", "mkv", "wmv", "flv",
                    "mp3", "wav", "flac", "aac", "ogg",
                    "zip", "rar", "7z", "gz", "tar"
                ));
            } else {
                wrapper.eq(Asset::getFileType, ft.toLowerCase());
            }
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

        var records = page.getRecords().stream().map(asset -> {
            var tags = getTagsByAssetId(asset.getAssetId());
            var result = new HashMap<String, Object>();
            result.put("assetId", asset.getAssetId());
            result.put("assetName", asset.getAssetName());
            result.put("description", asset.getDescription());
            result.put("fileType", asset.getFileType());
            result.put("fileSize", asset.getFileSize());
            result.put("storagePath", asset.getStoragePath());
            result.put("uploadUserId", asset.getUploadUserId());
            result.put("categoryId", asset.getCategoryId());
            result.put("currentVersion", asset.getCurrentVersion());
            result.put("isDeleted", asset.getIsDeleted());
            result.put("createdAt", asset.getCreatedAt());
            result.put("updatedAt", asset.getUpdatedAt());
            result.put("tags", tags);
            return result;
        }).toList();

        Map<String, Object> result = new HashMap<>();
        result.put("total", page.getTotal());
        result.put("records", records);
        return Result.ok(result);
    }

    @Override
    public Result<?> getDetail(Long id) {
        var asset = assetMapper.selectById(id);
        if (asset == null) {
            return Result.error("资产不存在");
        }
        var tags = getTagsByAssetId(id);

        var categoryPath = getCategoryPath(asset.getCategoryId());

        Map<String, Object> data = new HashMap<>();
        data.put("assetId", asset.getAssetId());
        data.put("assetName", asset.getAssetName());
        data.put("description", asset.getDescription());
        data.put("fileType", asset.getFileType());
        data.put("fileSize", asset.getFileSize());
        data.put("storagePath", asset.getStoragePath());
        data.put("uploadUserId", asset.getUploadUserId());
        data.put("categoryId", asset.getCategoryId());
        data.put("currentVersion", asset.getCurrentVersion());
        data.put("isDeleted", asset.getIsDeleted());
        data.put("createdAt", asset.getCreatedAt());
        data.put("updatedAt", asset.getUpdatedAt());
        data.put("tags", tags);
        data.put("categoryPath", categoryPath);
        return Result.ok(data);
    }

    private List<String> getCategoryPath(Long categoryId) {
        if (categoryId == null) {
            return List.of();
        }
        List<String> path = new ArrayList<>();
        var currentId = categoryId;
        while (currentId != null) {
            var category = categoryMapper.selectById(currentId);
            if (category == null) {
                break;
            }
            path.add(0, category.getCategoryName());
            currentId = category.getParentId();
        }
        return path;
    }

    @Override
    @Transactional
    public Result<?> update(Long id, UpdateAssetDTO dto, Long userId, String ipAddress) {
        var existAsset = assetMapper.selectById(id);
        if (existAsset == null) {
            return Result.error("资产不存在");
        }

        existAsset.setAssetName(dto.getName());
        existAsset.setCategoryId(dto.getCategoryId());
        existAsset.setDescription(dto.getDescription());
        assetMapper.updateById(existAsset);

        List<Long> tagIds = dto.getTagIds();
        assetTagMapper.delete(new LambdaQueryWrapper<AssetTag>().eq(AssetTag::getAssetId, id));
        if (tagIds != null && !tagIds.isEmpty()) {
            for (var tagId : tagIds) {
                var assetTag = new AssetTag();
                assetTag.setAssetId(id);
                assetTag.setTagId(tagId);
                assetTagMapper.insert(assetTag);
            }
        }

        var log = new OperationLog();
        log.setUserId(userId);
        log.setOperationType("UPDATE");
        log.setDescription("更新资产: " + existAsset.getAssetName());
        log.setIpAddress(ipAddress);
        operationLogMapper.insert(log);

        return Result.ok("更新成功");
    }

    @Override
    public Result<?> softDelete(Long id, Long userId, String ipAddress) {
        var asset = assetMapper.selectById(id);
        if (asset == null) {
            return Result.error("资产不存在");
        }
        asset.setIsDeleted(1);
        assetMapper.updateById(asset);

        var log = new OperationLog();
        log.setUserId(userId);
        log.setOperationType("DELETE");
        log.setDescription("删除资产: " + asset.getAssetName());
        log.setIpAddress(ipAddress);
        operationLogMapper.insert(log);

        return Result.ok("已移入回收站");
    }

    @Override
    public Result<?> restore(Long id, Long userId, String ipAddress) {
        var asset = assetMapper.selectById(id);
        if (asset == null) {
            return Result.error("资产不存在");
        }
        asset.setIsDeleted(0);
        assetMapper.updateById(asset);

        var log = new OperationLog();
        log.setUserId(userId);
        log.setOperationType("RESTORE");
        log.setDescription("恢复资产: " + asset.getAssetName());
        log.setIpAddress(ipAddress);
        operationLogMapper.insert(log);

        return Result.ok("已恢复");
    }

    @Override
    @Transactional
    public Result<?> permanentDelete(Long id, Long userId, String ipAddress) {
        var asset = assetMapper.selectById(id);
        if (asset == null) {
            return Result.error("资产不存在");
        }

        FileUtil.deleteFile(uploadPath, asset.getStoragePath());

        var versions = assetVersionMapper.selectList(
                new LambdaQueryWrapper<AssetVersion>().eq(AssetVersion::getAssetId, id));
        for (var version : versions) {
            FileUtil.deleteFile(uploadPath, version.getStoragePath());
        }
        assetVersionMapper.delete(new LambdaQueryWrapper<AssetVersion>().eq(AssetVersion::getAssetId, id));

        assetTagMapper.delete(new LambdaQueryWrapper<AssetTag>().eq(AssetTag::getAssetId, id));

        assetMapper.deleteById(id);

        var log = new OperationLog();
        log.setUserId(userId);
        log.setOperationType("PERMANENT_DELETE");
        log.setDescription("永久删除资产: " + asset.getAssetName());
        log.setIpAddress(ipAddress);
        operationLogMapper.insert(log);

        return Result.ok("已永久删除");
    }

    @Override
    public Result<?> download(Long id, Long userId, String ipAddress) {
        var asset = assetMapper.selectById(id);
        if (asset == null) {
            return Result.error("资产不存在");
        }

        try {
            var fileBytes = FileUtil.readFile(uploadPath, asset.getStoragePath());
            var downloadLog = new DownloadLog();
            downloadLog.setAssetId(id);
            downloadLog.setUserId(userId);
            downloadLog.setIpAddress(ipAddress);
            downloadLogMapper.insert(downloadLog);

            var ext = FileUtil.getExtension(asset.getStoragePath());
            var downloadName = asset.getAssetName();
            if (downloadName != null && !downloadName.toLowerCase().endsWith(ext.toLowerCase())) {
                downloadName = downloadName + ext;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("fileName", downloadName);
            data.put("fileBytes", fileBytes);
            data.put("fileType", asset.getFileType());
            return Result.ok(data);
        } catch (IOException e) {
            return Result.error("文件读取失败");
        }
    }

    @Override
    public Result<?> getRecycleList(int page, int pageSize) {
        var wrapper = new LambdaQueryWrapper<Asset>()
                .eq(Asset::getIsDeleted, 1)
                .orderByDesc(Asset::getUpdatedAt);
        var pageResult = assetMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return Result.ok(pageResult);
    }

    private List<Tag> getTagsByAssetId(Long assetId) {
        var tagIds = assetTagMapper.selectList(
                        new LambdaQueryWrapper<AssetTag>().eq(AssetTag::getAssetId, assetId))
                .stream()
                .map(AssetTag::getTagId)
                .toList();
        if (tagIds.isEmpty()) {
            return List.of();
        }
        return tagMapper.selectList(
                new LambdaQueryWrapper<Tag>().in(Tag::getTagId, tagIds));
    }
}
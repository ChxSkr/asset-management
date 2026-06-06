package com.dam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dam.common.Result;
import com.dam.entity.Asset;
import com.dam.entity.AssetVersion;
import com.dam.entity.OperationLog;
import com.dam.mapper.AssetMapper;
import com.dam.mapper.AssetVersionMapper;
import com.dam.mapper.OperationLogMapper;
import com.dam.service.VersionService;
import com.dam.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class VersionServiceImpl implements VersionService {

    @Value("${file.upload-path}")
    private String uploadPath;

    private final AssetMapper assetMapper;
    private final AssetVersionMapper assetVersionMapper;
    private final OperationLogMapper operationLogMapper;

    @Override
    @Transactional
    public Result<?> uploadNewVersion(Long assetId, MultipartFile file, String note, Long userId, String ipAddress) {
        var asset = assetMapper.selectById(assetId);
        if (asset == null) {
            return Result.error("资产不存在");
        }

        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        if (!FileUtil.isAllowedExtension(file.getOriginalFilename())) {
            return Result.error("不支持的文件格式");
        }
        if (!FileUtil.isSizeValid(file.getSize())) {
            return Result.error("文件大小超出限制");
        }

        var storageFilename = FileUtil.generateStorageFilename(file.getOriginalFilename());
        try {
            FileUtil.saveFile(file.getBytes(), uploadPath, storageFilename);
        } catch (IOException e) {
            return Result.error("文件保存失败");
        }

        var newVersionNumber = asset.getCurrentVersion() != null ? asset.getCurrentVersion() + 1 : 2;

        var version = new AssetVersion();
        version.setAssetId(assetId);
        version.setVersionNumber(newVersionNumber);
        version.setFileSize(file.getSize());
        version.setStoragePath(storageFilename);
        version.setUploadUserId(userId);
        version.setVersionNote(note != null ? note : "版本 " + newVersionNumber);
        assetVersionMapper.insert(version);

        asset.setCurrentVersion(newVersionNumber);
        asset.setFileSize(file.getSize());
        asset.setStoragePath(storageFilename);
        assetMapper.updateById(asset);

        var log = new OperationLog();
        log.setUserId(userId);
        log.setOperationType("VERSION_UPLOAD");
        log.setDescription("上传新版本: " + asset.getAssetName() + " (v" + newVersionNumber + ")");
        log.setIpAddress(ipAddress);
        operationLogMapper.insert(log);

        return Result.ok("新版本上传成功", version);
    }

    @Override
    public Result<?> getVersionHistory(Long assetId) {
        var asset = assetMapper.selectById(assetId);
        if (asset == null) {
            return Result.error("资产不存在");
        }
        var versions = assetVersionMapper.selectList(
                new LambdaQueryWrapper<AssetVersion>()
                        .eq(AssetVersion::getAssetId, assetId)
                        .orderByDesc(AssetVersion::getVersionNumber));
        return Result.ok(versions);
    }
}
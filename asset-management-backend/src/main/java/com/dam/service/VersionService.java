package com.dam.service;

import com.dam.common.Result;
import org.springframework.web.multipart.MultipartFile;

public interface VersionService {
    Result<?> uploadNewVersion(Long assetId, MultipartFile file, String note, Long userId, String ipAddress);
    Result<?> getVersionHistory(Long assetId);
}
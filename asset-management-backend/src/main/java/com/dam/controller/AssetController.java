package com.dam.controller;

import com.dam.common.Result;
import com.dam.dto.SearchDTO;
import com.dam.dto.UpdateAssetDTO;
import com.dam.entity.Asset;
import com.dam.service.AssetService;
import com.dam.service.VersionService;
import com.dam.util.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/asset")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;
    private final VersionService versionService;

    @PostMapping("/upload")
    public Result<?> upload(HttpServletRequest request,
                             @RequestParam("file") MultipartFile file,
                             @RequestParam(value = "name", required = false) String name,
                             @RequestParam(value = "categoryId", required = false) Long categoryId,
                             @RequestParam(value = "tagIds", required = false) List<Long> tagIds,
                             @RequestParam(value = "description", required = false) String description) {
        var userId = (Long) request.getAttribute("userId");
        var ip = IpUtil.getIpAddress(request);
        return assetService.upload(file, name, categoryId, tagIds, description, userId, ip);
    }

    @GetMapping("/list")
    public Result<?> getList(SearchDTO dto) {
        return assetService.getList(dto);
    }

    @GetMapping("/{id}")
    public Result<?> getDetail(@PathVariable Long id) {
        return assetService.getDetail(id);
    }

    @PutMapping("/{id}")
    public Result<?> update(HttpServletRequest request,
                             @PathVariable Long id,
                             @RequestBody UpdateAssetDTO dto) {
        var userId = (Long) request.getAttribute("userId");
        var ip = IpUtil.getIpAddress(request);
        return assetService.update(id, dto, userId, ip);
    }

    @DeleteMapping("/{id}")
    public Result<?> softDelete(HttpServletRequest request, @PathVariable Long id) {
        var userId = (Long) request.getAttribute("userId");
        var ip = IpUtil.getIpAddress(request);
        return assetService.softDelete(id, userId, ip);
    }

    @GetMapping("/{id}/download")
    public void download(HttpServletRequest request,
                         HttpServletResponse response,
                         @PathVariable Long id) throws IOException {
        var userId = (Long) request.getAttribute("userId");
        var ip = IpUtil.getIpAddress(request);
        var result = assetService.download(id, userId, ip);
        if (result.getCode() != 200) {
            response.setStatus(result.getCode());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":" + result.getCode() + ",\"message\":\"" + result.getMessage() + "\"}");
            return;
        }
        @SuppressWarnings("unchecked")
        var data = (Map<String, Object>) result.getData();
        var fileName = (String) data.get("fileName");
        var fileBytes = (byte[]) data.get("fileBytes");
        var fileType = (String) data.get("fileType");

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" +
                new String(fileName.getBytes(java.nio.charset.StandardCharsets.UTF_8), java.nio.charset.StandardCharsets.ISO_8859_1) + "\"");
        response.setContentLength(fileBytes.length);
        response.getOutputStream().write(fileBytes);
        response.getOutputStream().flush();
    }

    @PostMapping("/{id}/version")
    public Result<?> uploadNewVersion(HttpServletRequest request,
                                       @PathVariable Long id,
                                       @RequestParam("file") MultipartFile file,
                                       @RequestParam(value = "note", required = false) String note) {
        var userId = (Long) request.getAttribute("userId");
        var ip = IpUtil.getIpAddress(request);
        return versionService.uploadNewVersion(id, file, note, userId, ip);
    }

    @GetMapping("/{id}/versions")
    public Result<?> getVersionHistory(@PathVariable Long id) {
        return versionService.getVersionHistory(id);
    }

    @GetMapping("/recycle")
    public Result<?> getRecycleList(@RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "10") int pageSize) {
        return assetService.getRecycleList(page, pageSize);
    }

    @PutMapping("/{id}/restore")
    public Result<?> restore(HttpServletRequest request, @PathVariable Long id) {
        var userId = (Long) request.getAttribute("userId");
        var ip = IpUtil.getIpAddress(request);
        return assetService.restore(id, userId, ip);
    }

    @DeleteMapping("/{id}/permanent")
    public Result<?> permanentDelete(HttpServletRequest request, @PathVariable Long id) {
        var userId = (Long) request.getAttribute("userId");
        var ip = IpUtil.getIpAddress(request);
        return assetService.permanentDelete(id, userId, ip);
    }
}
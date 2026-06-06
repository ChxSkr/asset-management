package com.dam.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dam.common.Result;
import com.dam.entity.DownloadLog;
import com.dam.entity.OperationLog;
import com.dam.mapper.AssetMapper;
import com.dam.mapper.DownloadLogMapper;
import com.dam.mapper.OperationLogMapper;
import com.dam.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/log")
@RequiredArgsConstructor
public class LogController {

    private final OperationLogMapper operationLogMapper;
    private final DownloadLogMapper downloadLogMapper;
    private final UserMapper userMapper;
    private final AssetMapper assetMapper;

    @GetMapping("/operation")
    public Result<?> getOperationLogs(HttpServletRequest request,
                                       @RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int pageSize) {
        var role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限");
        }
        var pageResult = operationLogMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<OperationLog>().orderByDesc(OperationLog::getOperationTime));
        
        List<Map<String, Object>> records = new ArrayList<>();
        for (var log : pageResult.getRecords()) {
            Map<String, Object> record = new HashMap<>();
            record.put("logId", log.getLogId());
            record.put("userId", log.getUserId());
            record.put("operationType", log.getOperationType());
            record.put("description", log.getDescription());
            record.put("operationTime", log.getOperationTime());
            record.put("ipAddress", log.getIpAddress());
            
            var user = userMapper.selectById(log.getUserId());
            if (user != null) {
                record.put("userName", user.getUsername());
            } else {
                record.put("userName", "未知用户");
            }
            
            records.add(record);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", pageResult.getTotal());
        result.put("current", pageResult.getCurrent());
        result.put("size", pageResult.getSize());
        
        return Result.ok(result);
    }

    @GetMapping("/download")
    public Result<?> getDownloadLogs(HttpServletRequest request,
                                      @RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int pageSize) {
        var role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限");
        }
        var pageResult = downloadLogMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<DownloadLog>().orderByDesc(DownloadLog::getDownloadTime));
        
        List<Map<String, Object>> records = new ArrayList<>();
        for (var log : pageResult.getRecords()) {
            Map<String, Object> record = new HashMap<>();
            record.put("logId", log.getLogId());
            record.put("assetId", log.getAssetId());
            record.put("userId", log.getUserId());
            record.put("downloadTime", log.getDownloadTime());
            record.put("ipAddress", log.getIpAddress());
            
            var user = userMapper.selectById(log.getUserId());
            if (user != null) {
                record.put("userName", user.getUsername());
            } else {
                record.put("userName", "未知用户");
            }
            
            var asset = assetMapper.selectById(log.getAssetId());
            if (asset != null) {
                record.put("assetName", asset.getAssetName());
            } else {
                record.put("assetName", "未知资产");
            }
            
            records.add(record);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", pageResult.getTotal());
        result.put("current", pageResult.getCurrent());
        result.put("size", pageResult.getSize());
        
        return Result.ok(result);
    }
}
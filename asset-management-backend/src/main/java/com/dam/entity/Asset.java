package com.dam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("asset")
public class Asset {
    @TableId(type = IdType.AUTO)
    private Long assetId;
    private String assetName;
    private String description;
    private String fileType;
    private Long fileSize;
    private String storagePath;
    private Long uploadUserId;
    private Long categoryId;
    private Integer currentVersion;
    private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
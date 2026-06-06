package com.dam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("asset_version")
public class AssetVersion {
    @TableId(type = IdType.AUTO)
    private Long versionId;
    private Long assetId;
    private Integer versionNumber;
    private Long fileSize;
    private String storagePath;
    private Long uploadUserId;
    private String versionNote;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
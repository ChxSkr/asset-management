package com.dam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("download_log")
public class DownloadLog {
    @TableId(type = IdType.AUTO)
    private Long logId;
    private Long assetId;
    private Long userId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime downloadTime;
    private String ipAddress;
}
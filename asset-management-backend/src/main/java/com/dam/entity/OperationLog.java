package com.dam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLog {
    @TableId(type = IdType.AUTO)
    private Long logId;
    private Long userId;
    private String operationType;
    private String description;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime operationTime;
    private String ipAddress;
}
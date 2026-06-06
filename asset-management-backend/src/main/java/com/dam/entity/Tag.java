package com.dam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tag")
public class Tag {
    @TableId(type = IdType.AUTO)
    private Long tagId;
    private String tagName;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
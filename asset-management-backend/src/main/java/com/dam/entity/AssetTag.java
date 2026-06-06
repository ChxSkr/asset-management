package com.dam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("asset_tag")
public class AssetTag {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long assetId;
    private Long tagId;
}
package com.dam.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateAssetDTO {
    private String name;
    private Long categoryId;
    private List<Long> tagIds;
    private String description;
}

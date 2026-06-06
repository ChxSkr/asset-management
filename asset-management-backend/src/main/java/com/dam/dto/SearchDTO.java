package com.dam.dto;

import lombok.Data;
import java.util.List;

@Data
public class SearchDTO {
    private String keyword;
    private Long categoryId;
    private List<Long> tagIds;
    private String fileType;
    private String startTime;
    private String endTime;
    private Integer page = 1;
    private Integer pageSize = 12;
}
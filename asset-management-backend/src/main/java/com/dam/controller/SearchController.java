package com.dam.controller;

import com.dam.common.Result;
import com.dam.dto.SearchDTO;
import com.dam.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public Result<?> search(SearchDTO dto) {
        return searchService.search(dto);
    }
}
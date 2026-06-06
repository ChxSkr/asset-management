package com.dam.service;

import com.dam.common.Result;
import com.dam.dto.SearchDTO;

public interface SearchService {
    Result<?> search(SearchDTO dto);
}
package com.dam.service;

import com.dam.common.Result;

public interface TagService {
    Result<?> getList();
    Result<?> add(String tagName);
    Result<?> delete(Long id);
}
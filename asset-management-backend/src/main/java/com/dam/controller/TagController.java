package com.dam.controller;

import com.dam.common.Result;
import com.dam.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tag")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping("/list")
    public Result<?> getList() {
        return tagService.getList();
    }

    @PostMapping
    public Result<?> add(@RequestBody Map<String, String> body) {
        return tagService.add(body.get("tagName"));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        return tagService.delete(id);
    }
}
package com.uniconvert.backend.domain.category.controller;

import com.uniconvert.backend.domain.category.dto.response.CategoryResponse;
import com.uniconvert.backend.domain.category.service.CategoryService;
import com.uniconvert.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "카테고리 목록 조회", description = "기본 지출 카테고리 9종을 정렬 순서대로 반환한다.")
    @GetMapping
    public ApiResponse<List<CategoryResponse>> getCategories() {
        return ApiResponse.success(categoryService.getCategories());
    }
}

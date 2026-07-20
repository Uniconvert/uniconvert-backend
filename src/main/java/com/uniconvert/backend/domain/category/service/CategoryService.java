package com.uniconvert.backend.domain.category.service;

import com.uniconvert.backend.domain.category.dto.response.CategoryResponse;
import com.uniconvert.backend.domain.category.enums.CategoryType;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
public class CategoryService {

    public List<CategoryResponse> getCategories() {
        return Arrays.stream(CategoryType.values())
                .sorted(Comparator.comparingInt(CategoryType::getSortOrder))
                .map(CategoryResponse::from)
                .toList();
    }
}

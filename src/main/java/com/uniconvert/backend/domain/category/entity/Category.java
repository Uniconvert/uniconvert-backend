package com.uniconvert.backend.domain.category.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Entity
@Table(name = "category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "icon_key", length = 255)
    private String iconKey;

    @Column(name = "sort_order", nullable = false)
    private Byte sortOrder;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private Category(Long categoryId, String name, String iconKey, Byte sortOrder, boolean isActive) {
        this.categoryId = categoryId;
        this.name = name;
        this.iconKey = iconKey;
        this.sortOrder = sortOrder;
        this.isActive = isActive;
    }
}

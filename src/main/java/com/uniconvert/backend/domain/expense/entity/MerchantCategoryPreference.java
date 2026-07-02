package com.uniconvert.backend.domain.expense.entity;

import com.uniconvert.backend.domain.category.entity.Category;
import com.uniconvert.backend.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Entity
@Table(name = "merchant_category_perference")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MerchantCategoryPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "merchant_category_preference_id")
    private Long merchantCategoryPreferenceId;

    @Column(name = "merchant_name", nullable = false, length = 255)
    private String merchantName;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Builder
    private MerchantCategoryPreference(Long merchantCategoryPreferenceId, String merchantName, User user, Category category) {
        this.merchantCategoryPreferenceId = merchantCategoryPreferenceId;
        this.merchantName = merchantName;
        this.user = user;
        this.category = category;
    }
}

package com.uniconvert.backend.domain.pot.entity;

import com.uniconvert.backend.domain.user.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "pot",
        indexes = {
                @Index(name = "idx_pot_user_id", columnList = "user_id"),
                @Index(name = "idx_pot_user_archived", columnList = "user_id, is_archived")
        }
)
public class Pot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "goal_category", length = 30)
    private String goalCategory;

    @Column(
            name = "target_amount",
            nullable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal targetAmount;

    @Column(
            name = "saved_amount",
            nullable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal savedAmount = BigDecimal.ZERO;

    @Column(
            name = "monthly_allocation",
            nullable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal monthlyAllocation = BigDecimal.ZERO;

    @Column(name = "is_archived", nullable = false)
    private boolean archived = false;

    @Column(name = "display_order", nullable = false)
    private Long displayOrder = 0L;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Pot() {
    }

    private Pot(
            User user,
            String name,
            String goalCategory,
            BigDecimal targetAmount,
            BigDecimal monthlyAllocation,
            Long displayOrder
    ) {
        this.user = user;
        this.name = name;
        this.goalCategory = goalCategory;
        this.targetAmount = targetAmount;
        this.savedAmount = BigDecimal.ZERO;
        this.monthlyAllocation = monthlyAllocation;
        this.archived = false;
        this.displayOrder = displayOrder;
    }

    public static Pot create(
            User user,
            String name,
            String goalCategory,
            BigDecimal targetAmount,
            BigDecimal monthlyAllocation,
            Long displayOrder
    ) {
        return new Pot(
                user,
                name,
                goalCategory,
                targetAmount,
                monthlyAllocation,
                displayOrder
        );
    }

    public void update(
            String name,
            String goalCategory,
            BigDecimal targetAmount,
            BigDecimal monthlyAllocation,
            Long displayOrder
    ) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }

        if (goalCategory != null) {
            this.goalCategory = goalCategory.isBlank() ? null : goalCategory;
        }

        if (targetAmount != null) {
            this.targetAmount = targetAmount;
        }

        if (monthlyAllocation != null) {
            this.monthlyAllocation = monthlyAllocation;
        }

        if (displayOrder != null) {
            this.displayOrder = displayOrder;
        }
    }

    public void updateArchived(boolean archived) {
        this.archived = archived;
    }

    /**
     * PotAllocation 등록 또는 수정에 맞춰 현재 저축액을 변경한다.
     *
     * 예:
     * 기존 월 배정액 10만 원 → 수정된 월 배정액 15만 원
     * difference = +5만 원
     */
    public void changeSavedAmount(BigDecimal difference) {
        BigDecimal changedAmount = this.savedAmount.add(difference);

        if (changedAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Pot의 저장 금액은 0보다 작을 수 없습니다."
            );
        }

        this.savedAmount = changedAmount;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    public String getGoalCategory() {
        return goalCategory;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public BigDecimal getSavedAmount() {
        return savedAmount;
    }

    public BigDecimal getMonthlyAllocation() {
        return monthlyAllocation;
    }

    public boolean isArchived() {
        return archived;
    }

    public Long getDisplayOrder() {
        return displayOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
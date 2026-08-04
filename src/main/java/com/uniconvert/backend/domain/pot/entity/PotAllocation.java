package com.uniconvert.backend.domain.pot.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "pot_allocation",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_pot_allocation_pot_month",
                        columnNames = {"pot_id", "year_month"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_pot_allocation_pot_id",
                        columnList = "pot_id"
                ),
                @Index(
                        name = "idx_pot_allocation_year_month",
                        columnList = "year_month"
                )
        }
)
public class PotAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pot_id", nullable = false)
    private Pot pot;

    @Column(name = "`year_month`", nullable = false, length = 7)
    private String yearMonth;

    @Column(
            name = "amount",
            nullable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal amount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected PotAllocation() {
    }

    private PotAllocation(
            Pot pot,
            String yearMonth,
            BigDecimal amount
    ) {
        this.pot = pot;
        this.yearMonth = yearMonth;
        this.amount = amount;
    }

    public static PotAllocation create(
            Pot pot,
            String yearMonth,
            BigDecimal amount
    ) {
        return new PotAllocation(pot, yearMonth, amount);
    }

    public void updateAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public Pot getPot() {
        return pot;
    }

    public String getYearMonth() {
        return yearMonth;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
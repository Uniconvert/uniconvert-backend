package com.uniconvert.backend.domain.fixedexpense.entity;

import com.uniconvert.backend.domain.expense.entity.Expense;
import com.uniconvert.backend.domain.fixedexpense.enums.OccurrenceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "fixed_expense_occurrence")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FixedExpenseOccurrence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fixed_expense_occurrence_id")
    private Long fixedExpenseOccurrenceId;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OccurrenceStatus status = OccurrenceStatus.SCHEDULED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id")
    private Expense expense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixed_expense_id", nullable = false)
    private FixedExpense fixedExpense;

    @Builder
    private FixedExpenseOccurrence(
            Long fixedExpenseOccurrenceId,
            LocalDate scheduledDate,
            LocalDateTime generatedAt,
            OccurrenceStatus status,
            Expense expense,
            FixedExpense fixedExpense
    ) {
        this.fixedExpenseOccurrenceId = fixedExpenseOccurrenceId;
        this.scheduledDate = scheduledDate;
        this.generatedAt = generatedAt;
        this.status = status;
        this.expense = expense;
        this.fixedExpense = fixedExpense;
    }
}

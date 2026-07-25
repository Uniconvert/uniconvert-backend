package com.uniconvert.backend.domain.expense.service;

import com.uniconvert.backend.domain.budget.entity.Budget;
import com.uniconvert.backend.domain.budget.repository.BudgetRepository;
import com.uniconvert.backend.domain.category.enums.CategoryType;
import com.uniconvert.backend.domain.exchange.dto.response.ConversionResult;
import com.uniconvert.backend.domain.exchange.service.ExchangeRateService;
import com.uniconvert.backend.domain.expense.dto.request.ExpenseCreateRequest;
import com.uniconvert.backend.domain.expense.dto.request.ExpenseUpdateRequest;
import com.uniconvert.backend.domain.expense.dto.response.ExpenseListItemResponse;
import com.uniconvert.backend.domain.expense.dto.response.ExpenseResponse;
import com.uniconvert.backend.domain.expense.entity.Expense;
import com.uniconvert.backend.domain.expense.entity.RateSource;
import com.uniconvert.backend.domain.expense.repository.ExpenseRepository;
import com.uniconvert.backend.domain.user.entity.User;
import com.uniconvert.backend.domain.user.repository.UserRepository;
import com.uniconvert.backend.global.exception.CustomException;
import com.uniconvert.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;
    private final ExchangeRateService exchangeRateService;

    // 지출 등록
    @Transactional
    public ExpenseResponse createExpense(Long userId, ExpenseCreateRequest request) {
        User user = getUser(userId);
        validateCategory(request.categoryId());

        LocalDate rateDate = request.spentAt().toLocalDate();
        ConversionResult conversion = exchangeRateService.getConversionRate(
                request.originalCurrency(), user.getHomeCurrencyCode(), rateDate
        );

        BigDecimal convertedAmountHome = request.originalAmount()
                .multiply(conversion.rate())
                .setScale(4, java.math.RoundingMode.HALF_UP);

        Expense expense = new Expense(
                user,
                request.originalAmount(),
                request.originalCurrency(),
                conversion.rate(),
                RateSource.DAILY_AVERAGE,
                conversion.rateDate(),
                convertedAmountHome,
                request.merchantName(),
                normalizeMerchantName(request.merchantName()),
                request.memo(),
                request.spentAt(),
                request.potId(),
                request.categoryId()
        );

        Expense saved = expenseRepository.save(expense);
        return ExpenseResponse.from(saved);
    }

    // 지출 단건 조회
    @Transactional(readOnly = true)
    public ExpenseResponse getExpense(Long userId, Long expenseId) {
        Expense expense = getOwnedExpense(userId, expenseId);
        return ExpenseResponse.from(expense);
    }

    // 지출 목록 조회 (필터: 기간·카테고리, page=0&size=6)
    @Transactional(readOnly = true)
    public Page<ExpenseListItemResponse> getExpenses(Long userId, LocalDateTime startAt, LocalDateTime endAt,
                                                     Long categoryId, Pageable pageable) {
        return expenseRepository.findAllByFilter(userId, startAt, endAt, categoryId, pageable)
                .map(ExpenseListItemResponse::from);
    }

    // 최근 지출 (홈 화면용)
    @Transactional(readOnly = true)
    public List<ExpenseListItemResponse> getRecentExpenses(Long userId) {
        return expenseRepository.findTop5ByUser_IdAndDeletedAtIsNullOrderBySpentAtDesc(userId)
                .stream()
                .map(ExpenseListItemResponse::from)
                .toList();
    }

    // 지출 수정 — 날짜·통화 변경 시 환율 재계산
    @Transactional
    public ExpenseResponse updateExpense(Long userId, Long expenseId, ExpenseUpdateRequest request) {
        Expense expense = getOwnedExpense(userId, expenseId);
        validateCategory(request.categoryId());

        boolean rateRecalculationNeeded =
                !expense.getOriginalCurrency().equals(request.originalCurrency())
                        || !expense.getSpentAt().toLocalDate().equals(request.spentAt().toLocalDate());

        expense.updateDetails(
                request.originalAmount(),
                request.originalCurrency(),
                request.merchantName(),
                request.memo(),
                request.spentAt(),
                request.potId(),
                request.categoryId()
        );

        if (rateRecalculationNeeded) {
            User user = expense.getUser();
            LocalDate rateDate = request.spentAt().toLocalDate();
            ConversionResult conversion = exchangeRateService.getConversionRate(
                    request.originalCurrency(), user.getHomeCurrencyCode(), rateDate
            );
            BigDecimal convertedAmountHome = request.originalAmount()
                    .multiply(conversion.rate())
                    .setScale(4, java.math.RoundingMode.HALF_UP);

            expense.updateRateInfo(conversion.rate(), RateSource.DAILY_AVERAGE,
                    conversion.rateDate(), convertedAmountHome);
        } else {
            // 통화·날짜는 그대로, 금액만 바뀐 경우 → 기존 환율로 환산 금액만 재계산
            BigDecimal convertedAmountHome = request.originalAmount()
                    .multiply(expense.getAppliedRate())
                    .setScale(4, java.math.RoundingMode.HALF_UP);
            expense.updateRateInfo(expense.getAppliedRate(), expense.getRateSource(),
                    expense.getRateDate(), convertedAmountHome);
        }

        return ExpenseResponse.from(expense);
    }

    // 지출 삭제 (소프트 삭제)
    @Transactional
    public void deleteExpense(Long userId, Long expenseId) {
        Expense expense = getOwnedExpense(userId, expenseId);
        expense.softDelete();
    }

    // 남은 예산 계산 = 월 예산 − 이번 달 Pot 배정 합계 − 이번 달 지출 합계
    // ⚠️ Pot 도메인 미구현으로 배정 합계는 임시 0 처리. Pot 생기면 potAllocationRepository 연동 필요
    @Transactional(readOnly = true)
    public BigDecimal getRemainingBudget(Long userId, YearMonth yearMonth) {
        String ym = yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        Budget budget = budgetRepository.findByUser_IdAndYearMonth(userId, ym)
                .orElseThrow(() -> new CustomException(ErrorCode.BUDGET_NOT_FOUND));

        LocalDateTime startAt = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endAt = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        BigDecimal totalExpense = expenseRepository.sumConvertedAmountByPeriod(userId, startAt, endAt);

        BigDecimal potAllocationTotal = BigDecimal.ZERO; // TODO: Pot 도메인 연동 후 교체

        return budget.getMonthlyLimitHome()
                .subtract(potAllocationTotal)
                .subtract(totalExpense);
    }

    private Expense getOwnedExpense(Long userId, Long expenseId) {
        return expenseRepository.findByIdAndUser_IdAndDeletedAtIsNull(expenseId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.EXPENSE_NOT_FOUND));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
    }

    private void validateCategory(Long categoryId) {
        if (!CategoryType.isValid(categoryId)) {
            throw new CustomException(ErrorCode.INVALID_CATEGORY);
        }
    }

    // 상점명 자동추천용 정규화 (소문자, 공백 제거 정도의 단순 처리 — 필요시 고도화)
    private String normalizeMerchantName(String merchantName) {
        return merchantName == null ? null : merchantName.trim().toLowerCase();
    }
}
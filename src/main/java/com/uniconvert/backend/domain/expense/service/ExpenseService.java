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
import com.uniconvert.backend.domain.pot.repository.PotAllocationRepository;
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
    private final PotAllocationRepository potAllocationRepository;

    // 지출 등록
    @Transactional
    public ExpenseResponse createExpense(
            Long userId,
            ExpenseCreateRequest request
    ) {
        User user = getUser(userId);
        validateCategory(request.categoryId());

        LocalDate rateDate = request.spentAt().toLocalDate();

        ConversionResult conversion =
                exchangeRateService.getConversionRate(
                        request.originalCurrency(),
                        user.getHomeCurrencyCode(),
                        rateDate
                );

        BigDecimal convertedAmountHome =
                request.originalAmount()
                        .multiply(conversion.rate())
                        .setScale(
                                4,
                                java.math.RoundingMode.HALF_UP
                        );

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
    public ExpenseResponse getExpense(
            Long userId,
            Long expenseId
    ) {
        Expense expense =
                getOwnedExpense(userId, expenseId);

        return ExpenseResponse.from(expense);
    }

    // 지출 목록 조회
    // 필터: 기간, 카테고리 / page=0, size=6
    @Transactional(readOnly = true)
    public Page<ExpenseListItemResponse> getExpenses(
            Long userId,
            LocalDateTime startAt,
            LocalDateTime endAt,
            Long categoryId,
            Pageable pageable
    ) {
        return expenseRepository
                .findAllByFilter(
                        userId,
                        startAt,
                        endAt,
                        categoryId,
                        pageable
                )
                .map(ExpenseListItemResponse::from);
    }

    // 최근 지출 조회
    // 홈 화면용
    @Transactional(readOnly = true)
    public List<ExpenseListItemResponse> getRecentExpenses(
            Long userId
    ) {
        return expenseRepository
                .findTop5ByUser_IdAndDeletedAtIsNullOrderBySpentAtDesc(
                        userId
                )
                .stream()
                .map(ExpenseListItemResponse::from)
                .toList();
    }

    // 지출 수정
    // 날짜 또는 통화가 변경되면 환율을 다시 계산합니다.
    @Transactional
    public ExpenseResponse updateExpense(
            Long userId,
            Long expenseId,
            ExpenseUpdateRequest request
    ) {
        Expense expense =
                getOwnedExpense(userId, expenseId);

        validateCategory(request.categoryId());

        boolean rateRecalculationNeeded =
                !expense.getOriginalCurrency()
                        .equals(request.originalCurrency())
                        || !expense.getSpentAt()
                        .toLocalDate()
                        .equals(request.spentAt().toLocalDate());

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

            LocalDate rateDate =
                    request.spentAt().toLocalDate();

            ConversionResult conversion =
                    exchangeRateService.getConversionRate(
                            request.originalCurrency(),
                            user.getHomeCurrencyCode(),
                            rateDate
                    );

            BigDecimal convertedAmountHome =
                    request.originalAmount()
                            .multiply(conversion.rate())
                            .setScale(
                                    4,
                                    java.math.RoundingMode.HALF_UP
                            );

            expense.updateRateInfo(
                    conversion.rate(),
                    RateSource.DAILY_AVERAGE,
                    conversion.rateDate(),
                    convertedAmountHome
            );
        } else {
            // 통화와 날짜는 그대로이고 금액만 변경된 경우
            // 기존 환율을 이용해 홈 통화 금액만 다시 계산합니다.
            BigDecimal convertedAmountHome =
                    request.originalAmount()
                            .multiply(expense.getAppliedRate())
                            .setScale(
                                    4,
                                    java.math.RoundingMode.HALF_UP
                            );

            expense.updateRateInfo(
                    expense.getAppliedRate(),
                    expense.getRateSource(),
                    expense.getRateDate(),
                    convertedAmountHome
            );
        }

        return ExpenseResponse.from(expense);
    }

    // 지출 삭제
    // 실제 데이터를 삭제하지 않고 삭제 일시를 기록합니다.
    @Transactional
    public void deleteExpense(
            Long userId,
            Long expenseId
    ) {
        Expense expense =
                getOwnedExpense(userId, expenseId);

        expense.softDelete();
    }

    // 사용 가능 금액 계산
    // 월 예산 - 해당 월 지출 합계 - 해당 월 Pot 배정 합계
    @Transactional(readOnly = true)
    public BigDecimal getRemainingBudget(
            Long userId,
            YearMonth yearMonth
    ) {
        String ym = yearMonth.format(
                DateTimeFormatter.ofPattern("yyyy-MM")
        );

        Budget budget = budgetRepository
                .findByUserIdAndYearMonth(userId, ym)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.BUDGET_NOT_FOUND
                        )
                );

        LocalDateTime startAt =
                yearMonth.atDay(1)
                        .atStartOfDay();

        LocalDateTime endAt =
                yearMonth.atEndOfMonth()
                        .atTime(23, 59, 59);

        BigDecimal totalExpense =
                expenseRepository
                        .sumConvertedAmountByPeriod(
                                userId,
                                startAt,
                                endAt
                        );

        if (totalExpense == null) {
            totalExpense = BigDecimal.ZERO;
        }

        BigDecimal potAllocationTotal =
                potAllocationRepository
                        .sumAmountByUserIdAndYearMonth(
                                userId,
                                ym
                        );

        if (potAllocationTotal == null) {
            potAllocationTotal = BigDecimal.ZERO;
        }

        return budget.getMonthlyLimitHome()
                .subtract(totalExpense)
                .subtract(potAllocationTotal);
    }

    private Expense getOwnedExpense(
            Long userId,
            Long expenseId
    ) {
        return expenseRepository
                .findByIdAndUser_IdAndDeletedAtIsNull(
                        expenseId,
                        userId
                )
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.EXPENSE_NOT_FOUND
                        )
                );
    }

    private User getUser(Long userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.NOT_FOUND
                        )
                );
    }

    private void validateCategory(Long categoryId) {
        if (!CategoryType.isValid(categoryId)) {
            throw new CustomException(
                    ErrorCode.INVALID_CATEGORY
            );
        }
    }

    // 상점명 자동 추천용 정규화
    // 소문자로 변환하고 앞뒤 공백을 제거합니다.
    private String normalizeMerchantName(
            String merchantName
    ) {
        return merchantName == null
                ? null
                : merchantName.trim().toLowerCase();
    }
}
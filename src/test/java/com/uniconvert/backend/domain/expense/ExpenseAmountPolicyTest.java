package com.uniconvert.backend.domain.expense;

import com.uniconvert.backend.domain.budget.repository.BudgetRepository;
import com.uniconvert.backend.domain.category.enums.CategoryType;
import com.uniconvert.backend.domain.exchange.dto.response.ConversionResult;
import com.uniconvert.backend.domain.exchange.service.ExchangeRateService;
import com.uniconvert.backend.domain.expense.dto.request.ExpenseCreateRequest;
import com.uniconvert.backend.domain.expense.dto.response.ExpenseResponse;
import com.uniconvert.backend.domain.expense.repository.ExpenseRepository;
import com.uniconvert.backend.domain.expense.service.ExpenseService;
import com.uniconvert.backend.domain.pot.repository.PotAllocationRepository;
import com.uniconvert.backend.domain.user.entity.User;
import com.uniconvert.backend.domain.user.repository.UserRepository;
import com.uniconvert.backend.global.exception.CustomException;
import com.uniconvert.backend.global.exception.ErrorCode;
import com.uniconvert.backend.global.uni.service.UniInsightMessageFactory;
import com.uniconvert.backend.global.uni.service.UniMessageService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExpenseAmountPolicyTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void rejects_zero_and_negative_original_amounts() {
        assertOriginalAmountInvalid(BigDecimal.ZERO);
        assertOriginalAmountInvalid(new BigDecimal("-1"));
    }

    @Test
    void rejects_original_amount_with_more_than_fifteen_integer_digits() {
        Set<ConstraintViolation<ExpenseCreateRequest>> violations =
                validator.validate(request(new BigDecimal("1000000000000000")));

        assertTrue(violations.stream().anyMatch(violation ->
                violation.getPropertyPath().toString().equals("originalAmount")));
    }

    @Test
    void rejects_original_amount_with_more_than_four_fraction_digits() {
        Set<ConstraintViolation<ExpenseCreateRequest>> violations =
                validator.validate(request(new BigDecimal("100.12345")));

        assertTrue(violations.stream().anyMatch(violation ->
                violation.getPropertyPath().toString().equals("originalAmount")));
    }

    @Test
    void accepts_the_database_aligned_original_amount_limit() {
        Set<ConstraintViolation<ExpenseCreateRequest>> violations =
                validator.validate(request(new BigDecimal("999999999999999.9999")));

        assertTrue(violations.isEmpty());
    }

    @Test
    void rejects_when_converted_amount_exceeds_database_range_before_save() {
        ExpenseRepository expenseRepository = mock(ExpenseRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ExchangeRateService exchangeRateService = mock(ExchangeRateService.class);
        ExpenseService service = service(expenseRepository, userRepository, exchangeRateService);
        LocalDateTime spentAt = LocalDateTime.of(2026, 8, 20, 12, 0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User("test@example.com", "password", "tester")));
        when(exchangeRateService.getConversionRate("USD", "KRW", spentAt.toLocalDate()))
                .thenReturn(new ConversionResult(new BigDecimal("1400"), spentAt.toLocalDate()));

        CustomException exception = assertThrows(
                CustomException.class,
                () -> service.createExpense(1L, new ExpenseCreateRequest(
                        new BigDecimal("999999999999999"),
                        "USD",
                        spentAt,
                        CategoryType.FOOD.getCategoryId(),
                        "Test merchant",
                        null,
                        null
                ))
        );

        assertEquals(ErrorCode.EXPENSE_AMOUNT_OUT_OF_RANGE, exception.getErrorCode());
        verify(expenseRepository, never()).save(any());
    }

    @Test
    void saves_when_converted_amount_is_within_database_range() {
        ExpenseRepository expenseRepository = mock(ExpenseRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ExchangeRateService exchangeRateService = mock(ExchangeRateService.class);
        ExpenseService service = service(expenseRepository, userRepository, exchangeRateService);
        LocalDateTime spentAt = LocalDateTime.of(2026, 8, 20, 12, 0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User("test@example.com", "password", "tester")));
        when(exchangeRateService.getConversionRate("USD", "KRW", spentAt.toLocalDate()))
                .thenReturn(new ConversionResult(new BigDecimal("1400"), spentAt.toLocalDate()));
        when(expenseRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ExpenseResponse response = service.createExpense(1L, new ExpenseCreateRequest(
                new BigDecimal("100"),
                "USD",
                spentAt,
                CategoryType.FOOD.getCategoryId(),
                "Test merchant",
                null,
                null
        ));

        assertEquals(new BigDecimal("140000.0000"), response.convertedAmountHome());
        assertEquals(new BigDecimal("100"), response.originalAmount());
    }

    private void assertOriginalAmountInvalid(BigDecimal amount) {
        Set<ConstraintViolation<ExpenseCreateRequest>> violations =
                validator.validate(request(amount));

        assertTrue(violations.stream().anyMatch(violation ->
                violation.getPropertyPath().toString().equals("originalAmount")));
    }

    private ExpenseCreateRequest request(BigDecimal amount) {
        return new ExpenseCreateRequest(
                amount,
                "USD",
                LocalDateTime.of(2026, 8, 20, 12, 0),
                CategoryType.FOOD.getCategoryId(),
                null,
                null,
                null
        );
    }

    private ExpenseService service(
            ExpenseRepository expenseRepository,
            UserRepository userRepository,
            ExchangeRateService exchangeRateService
    ) {
        return new ExpenseService(
                expenseRepository,
                userRepository,
                mock(BudgetRepository.class),
                exchangeRateService,
                mock(PotAllocationRepository.class),
                mock(com.uniconvert.backend.domain.currency.service.CurrencyService.class),
                mock(UniInsightMessageFactory.class),
                mock(UniMessageService.class)
        );
    }
}

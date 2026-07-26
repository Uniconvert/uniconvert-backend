package com.uniconvert.backend.domain.pot.service;

import com.uniconvert.backend.domain.pot.dto.request.PotAllocationUpsertRequest;
import com.uniconvert.backend.domain.pot.dto.response.PotAllocationResponse;
import com.uniconvert.backend.domain.pot.entity.Pot;
import com.uniconvert.backend.domain.pot.entity.PotAllocation;
import com.uniconvert.backend.domain.pot.repository.PotAllocationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PotAllocationService {

    private final PotAllocationRepository allocationRepository;
    private final PotService potService;

    public PotAllocationService(
            PotAllocationRepository allocationRepository,
            PotService potService
    ) {
        this.allocationRepository = allocationRepository;
        this.potService = potService;
    }

    @Transactional
    public PotAllocationResponse upsert(
            Long userId,
            Long potId,
            PotAllocationUpsertRequest request
    ) {
        Pot pot = potService.getOwnedPot(userId, potId);

        if (pot.isArchived()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "보관된 Pot에는 금액을 배정할 수 없습니다."
            );
        }

        PotAllocation allocation =
                allocationRepository
                        .findByPot_IdAndYearMonth(
                                potId,
                                request.yearMonth()
                        )
                        .map(existing -> updateExisting(
                                pot,
                                existing,
                                request.amount()
                        ))
                        .orElseGet(() -> createNew(
                                pot,
                                request.yearMonth(),
                                request.amount()
                        ));

        PotAllocation savedAllocation =
                allocationRepository.save(allocation);

        return PotAllocationResponse.from(savedAllocation);
    }

    public List<PotAllocationResponse> getByPot(
            Long userId,
            Long potId
    ) {
        // 다른 사용자의 Pot 조회 방지
        potService.getOwnedPot(userId, potId);

        return allocationRepository
                .findAllByPot_IdOrderByYearMonthDesc(potId)
                .stream()
                .map(PotAllocationResponse::from)
                .toList();
    }

    public List<PotAllocationResponse> getByMonth(
            Long userId,
            String yearMonth
    ) {
        validateYearMonth(yearMonth);

        return allocationRepository
                .findAllByUserIdAndYearMonth(userId, yearMonth)
                .stream()
                .map(PotAllocationResponse::from)
                .toList();
    }

    public BigDecimal getTotalByMonth(
            Long userId,
            String yearMonth
    ) {
        validateYearMonth(yearMonth);

        return allocationRepository
                .sumAmountByUserIdAndYearMonth(
                        userId,
                        yearMonth
                );
    }

    private PotAllocation updateExisting(
            Pot pot,
            PotAllocation existing,
            BigDecimal newAmount
    ) {
        BigDecimal difference =
                newAmount.subtract(existing.getAmount());

        pot.changeSavedAmount(difference);
        existing.updateAmount(newAmount);

        return existing;
    }

    private PotAllocation createNew(
            Pot pot,
            String yearMonth,
            BigDecimal amount
    ) {
        pot.changeSavedAmount(amount);

        return PotAllocation.create(
                pot,
                yearMonth,
                amount
        );
    }

    private void validateYearMonth(String yearMonth) {
        if (yearMonth == null
                || !yearMonth.matches("^\\d{4}-(0[1-9]|1[0-2])$")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "연월은 YYYY-MM 형식이어야 합니다."
            );
        }
    }
}
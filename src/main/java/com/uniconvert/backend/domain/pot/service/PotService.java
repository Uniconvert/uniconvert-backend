package com.uniconvert.backend.domain.pot.service;

import com.uniconvert.backend.domain.pot.dto.request.PotArchiveRequest;
import com.uniconvert.backend.domain.pot.dto.request.PotCreateRequest;
import com.uniconvert.backend.domain.pot.dto.request.PotUpdateRequest;
import com.uniconvert.backend.domain.pot.dto.response.PotResponse;
import com.uniconvert.backend.domain.pot.entity.Pot;
import com.uniconvert.backend.domain.pot.entity.PotAllocation;
import com.uniconvert.backend.domain.pot.repository.PotAllocationRepository;
import com.uniconvert.backend.domain.pot.repository.PotRepository;
import com.uniconvert.backend.domain.pot.repository.projection.PotAmountProjection;
import com.uniconvert.backend.domain.user.entity.User;
import com.uniconvert.backend.domain.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PotService {

    /**
     * 사용자별 timezone 적용 전 기본값입니다.
     *
     * User 엔티티에 timezone이 있으면 추후 사용자 timezone 기준으로
     * 변경하는 것이 최종적으로 더 정확합니다.
     */
    private static final ZoneId DEFAULT_ZONE_ID =
            ZoneId.of("Asia/Seoul");

    private final PotRepository potRepository;
    private final PotAllocationRepository allocationRepository;
    private final UserRepository userRepository;

    public PotService(
            PotRepository potRepository,
            PotAllocationRepository allocationRepository,
            UserRepository userRepository
    ) {
        this.potRepository = potRepository;
        this.allocationRepository = allocationRepository;
        this.userRepository = userRepository;
    }

    private String getCurrentYearMonth(User user) {
        String timezone = user.getTimezone();

        if (timezone == null || timezone.isBlank()) {
            return YearMonth.now(
                    ZoneId.of("Asia/Seoul")
            ).toString();
        }

        try {
            return YearMonth.now(
                    ZoneId.of(timezone)
            ).toString();

        } catch (DateTimeException exception) {
            return YearMonth.now(
                    ZoneId.of("Asia/Seoul")
            ).toString();
        }
    }

    @Transactional
    public PotResponse create(
            Long userId,
            PotCreateRequest request
    ) {
        User user = getUser(userId);

        Long displayOrder = request.displayOrder();

        if (displayOrder == null) {
            displayOrder =
                    potRepository.findMaxDisplayOrderByUserId(userId) + 1;
        }

        Pot pot = Pot.create(
                user,
                request.name().trim(),
                normalizeNullableText(request.goalCategory()),
                normalizeNullableText(request.representativeImageKey()),
                request.targetAmount(),
                request.monthlyAllocation(),
                displayOrder
        );

        Pot savedPot = potRepository.save(pot);

        // 생성 직후에는 아직 실제 월별 배정 내역이 없으므로 0
        return PotResponse.from(
                savedPot,
                BigDecimal.ZERO
        );
    }

    /**
     * Pot 목록 조회
     *
     * 각 Pot의 현재 월 실제 배정 금액을 조회하여
     * PotResponse.thisMonthAmount에 넣습니다.
     */
    public List<PotResponse> getAll(
            Long userId,
            boolean includeArchived
    ) {
        User user = getUser(userId);

        String yearMonth =
                getCurrentYearMonth(user);

        List<Pot> pots;

        if (includeArchived) {
            pots =
                    potRepository
                            .findAllByUser_IdOrderByDisplayOrderAscIdAsc(
                                    userId
                            );
        } else {
            pots =
                    potRepository
                            .findAllByUser_IdAndArchivedFalseOrderByDisplayOrderAscIdAsc(
                                    userId
                            );
        }

        List<PotAmountProjection> monthAmounts =
                allocationRepository
                        .findPotAmountsByUserIdAndYearMonth(
                                userId,
                                yearMonth
                        );

        Map<Long, BigDecimal> thisMonthAmountMap =
                monthAmounts.stream()
                        .collect(Collectors.toMap(
                                PotAmountProjection::getPotId,
                                projection ->
                                        projection.getAmount() != null
                                                ? projection.getAmount()
                                                : BigDecimal.ZERO
                        ));

        return pots.stream()
                .map(pot ->
                        PotResponse.from(
                                pot,
                                thisMonthAmountMap.getOrDefault(
                                        pot.getId(),
                                        BigDecimal.ZERO
                                )
                        )
                )
                .toList();
    }

    /**
     * Pot 상세 조회
     */
    public PotResponse getOne(
            Long userId,
            Long potId
    ) {
        Pot pot = getOwnedPot(userId, potId);

        String yearMonth =
                getCurrentYearMonth(pot.getUser());

        BigDecimal thisMonthAmount =
                allocationRepository
                        .findByPot_IdAndYearMonth(
                                potId,
                                yearMonth
                        )
                        .map(PotAllocation::getAmount)
                        .orElse(BigDecimal.ZERO);

        return PotResponse.from(
                pot,
                thisMonthAmount
        );
    }

    @Transactional
    public PotResponse update(
            Long userId,
            Long potId,
            PotUpdateRequest request
    ) {
        Pot pot = getOwnedPot(userId, potId);

        pot.update(
                request.name() == null
                        ? null
                        : request.name().trim(),

                request.goalCategory() == null
                        ? null
                        : normalizeNullableText(
                        request.goalCategory()
                ),

                request.representativeImageKey() == null
                        ? null
                        : normalizeNullableText(
                        request.representativeImageKey()
                ),

                request.targetAmount(),
                request.monthlyAllocation(),
                request.displayOrder()
        );

        BigDecimal thisMonthAmount =
                getThisMonthAmount(potId);

        return PotResponse.from(
                pot,
                thisMonthAmount
        );
    }

    /**
     * 기존 보관 기능입니다.
     *
     * 주의:
     * 이 메서드는 Pot이나 PotAllocation을 실제 삭제하지 않으므로
     * 문서에서 요구한 '도중 삭제 후 이번 달 금액 복구'와는 다릅니다.
     */
    @Transactional
    public PotResponse updateArchived(
            Long userId,
            Long potId,
            PotArchiveRequest request
    ) {
        Pot pot = getOwnedPot(userId, potId);

        pot.updateArchived(request.archived());

        BigDecimal thisMonthAmount =
                getThisMonthAmount(potId);

        return PotResponse.from(
                pot,
                thisMonthAmount
        );
    }

    public Pot getOwnedPot(
            Long userId,
            Long potId
    ) {
        return potRepository
                .findByIdAndUser_Id(potId, userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Pot을 찾을 수 없습니다."
                        )
                );
    }

    private BigDecimal getThisMonthAmount(
            Long potId
    ) {
        return allocationRepository
                .findByPot_IdAndYearMonth(
                        potId,
                        getCurrentYearMonth()
                )
                .map(allocation ->
                        defaultZero(allocation.getAmount())
                )
                .orElse(BigDecimal.ZERO);
    }

    private String getCurrentYearMonth() {
        return YearMonth.now(DEFAULT_ZONE_ID)
                .toString();
    }

    private BigDecimal defaultZero(
            BigDecimal amount
    ) {
        return amount != null
                ? amount
                : BigDecimal.ZERO;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "사용자를 찾을 수 없습니다."
                        )
                );
    }

    private String normalizeNullableText(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
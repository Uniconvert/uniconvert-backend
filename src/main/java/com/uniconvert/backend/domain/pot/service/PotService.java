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
import com.uniconvert.backend.domain.currency.service.CurrencyService;
import com.uniconvert.backend.domain.pot.dto.response.PotDetailResponse;
import com.uniconvert.backend.global.uni.dto.UniMessageBundleResponse;
import com.uniconvert.backend.global.uni.dto.UniMessageResponse;
import com.uniconvert.backend.global.uni.enums.UniSection;
import com.uniconvert.backend.global.uni.service.UniInsightMessageFactory;
import com.uniconvert.backend.global.uni.service.UniMessageService;
import com.uniconvert.backend.domain.pot.dto.response.PotListResponse;

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

    private static final ZoneId DEFAULT_ZONE_ID =
            ZoneId.of("Asia/Seoul");

    private final PotRepository potRepository;
    private final PotAllocationRepository allocationRepository;
    private final UserRepository userRepository;
    private final CurrencyService currencyService;
    private final UniInsightMessageFactory uniInsightMessageFactory;
    private final UniMessageService uniMessageService;

    public PotService(
            PotRepository potRepository,
            PotAllocationRepository allocationRepository,
            UserRepository userRepository,
            CurrencyService currencyService,
            UniInsightMessageFactory uniInsightMessageFactory,
            UniMessageService uniMessageService
    ) {
        this.potRepository = potRepository;
        this.allocationRepository = allocationRepository;
        this.userRepository = userRepository;
        this.currencyService = currencyService;
        this.uniInsightMessageFactory = uniInsightMessageFactory;
        this.uniMessageService = uniMessageService;
    }

    /**
     * 사용자 timezone 기준 현재 연월 반환
     */
    private String getCurrentYearMonth(User user) {
        String timezone = user.getTimezone();

        if (timezone == null || timezone.isBlank()) {
            return YearMonth.now(DEFAULT_ZONE_ID).toString();
        }

        try {
            return YearMonth.now(
                    ZoneId.of(timezone)
            ).toString();

        } catch (DateTimeException exception) {
            return YearMonth.now(DEFAULT_ZONE_ID).toString();
        }
    }

    /**
     * Pot 생성
     *
     * Pot 생성과 동시에 monthlyAllocation 값을
     * 현재 월의 실제 PotAllocation으로 등록한다.
     *
     * 예:
     * monthlyAllocation = 300000
     * -> 현재 월 pot_allocation.amount = 300000
     * -> savedAmount = 300000
     */
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

        /*
         * Pot 생성 시 현재 월 실제 배정도 함께 생성한다.
         *
         * monthlyAllocation은 월 계획 금액이면서
         * 최초 생성 시에는 이번 달 실제 배정 금액으로 사용한다.
         */
        String currentYearMonth =
                getCurrentYearMonth(user);

        BigDecimal thisMonthAmount =
                defaultZero(request.monthlyAllocation());

        PotAllocation allocation =
                PotAllocation.create(
                        savedPot,
                        currentYearMonth,
                        thisMonthAmount
                );

        allocationRepository.save(allocation);

        /*
         * 모든 월 실제 배정 누적값인 savedAmount에도 반영한다.
         */
        savedPot.changeSavedAmount(thisMonthAmount);

        return PotResponse.from(
                savedPot,
                thisMonthAmount
        );
    }

    /**
     * Pot 목록 조회
     */
    public PotListResponse getAll(
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

        UniMessageBundleResponse uniMessages =
                uniMessageService.createBundle(
                        UniSection.POTS,
                        List.of()
                );

        if (pots.isEmpty()) {
            return new PotListResponse(
                    List.of(),
                    uniMessages
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

        List<PotResponse> potResponses =
                pots.stream()
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

        return new PotListResponse(
                potResponses,
                uniMessages
        );
    }

    /**
     * Pot 상세 조회
     */
    public PotDetailResponse getOne(
            Long userId,
            Long potId
    ) {
        Pot pot = getOwnedPot(userId, potId);

        BigDecimal thisMonthAmount =
                getThisMonthAmount(pot);

        String currencySymbol =
                currencyService.getSymbolByCode(
                        pot.getUser().getHomeCurrencyCode()
                );

        List<UniMessageResponse> insights =
                uniInsightMessageFactory.createPotInsights(
                        pot.getName(),
                        pot.getSavedAmount(),
                        pot.getTargetAmount(),
                        thisMonthAmount,
                        currencySymbol
                );

        UniMessageBundleResponse uniMessages =
                uniMessageService.createBundle(
                        UniSection.POTS,
                        insights
                );

        return new PotDetailResponse(
                PotResponse.from(
                        pot,
                        thisMonthAmount
                ),
                uniMessages
        );
    }

    /**
     * Pot 정보 수정
     *
     * monthlyAllocation 수정은 앞으로의 월 계획 금액 수정이다.
     * 이미 생성된 현재 월의 실제 PotAllocation은
     * /pots/{potId}/allocations API를 통해 별도로 수정한다.
     */
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
                getThisMonthAmount(pot);

        return PotResponse.from(
                pot,
                thisMonthAmount
        );
    }

    /**
     * Pot 보관 / 복구
     *
     * archived = true
     * -> Pot 보관
     * -> PotAllocation 데이터는 삭제하지 않는다.
     * -> 총 Pot 배정액 계산에서는 제외되어 사용 가능 금액이 복구된다.
     *
     * archived = false
     * -> Pot 복구
     * -> 기존 PotAllocation이 다시 총 배정액 계산에 포함된다.
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
                getThisMonthAmount(pot);

        return PotResponse.from(
                pot,
                thisMonthAmount
        );
    }

    /**
     * 로그인 사용자가 소유한 Pot 조회
     */
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

    /**
     * 해당 Pot 사용자 timezone 기준 이번 달 실제 배정 금액 조회
     */
    private BigDecimal getThisMonthAmount(
            Pot pot
    ) {
        String currentYearMonth =
                getCurrentYearMonth(pot.getUser());

        return allocationRepository
                .findByPot_IdAndYearMonth(
                        pot.getId(),
                        currentYearMonth
                )
                .map(allocation ->
                        defaultZero(allocation.getAmount())
                )
                .orElse(BigDecimal.ZERO);
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
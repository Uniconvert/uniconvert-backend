package com.uniconvert.backend.domain.pot.service;

import com.uniconvert.backend.domain.pot.dto.request.PotArchiveRequest;
import com.uniconvert.backend.domain.pot.dto.request.PotCreateRequest;
import com.uniconvert.backend.domain.pot.dto.request.PotUpdateRequest;
import com.uniconvert.backend.domain.pot.dto.response.PotResponse;
import com.uniconvert.backend.domain.pot.entity.Pot;
import com.uniconvert.backend.domain.pot.repository.PotRepository;
import com.uniconvert.backend.domain.user.entity.User;
import com.uniconvert.backend.domain.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PotService {

    private final PotRepository potRepository;
    private final UserRepository userRepository;

    public PotService(
            PotRepository potRepository,
            UserRepository userRepository
    ) {
        this.potRepository = potRepository;
        this.userRepository = userRepository;
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
                request.name(),
                normalizeNullableText(request.goalCategory()),
                request.targetAmount(),
                request.monthlyAllocation(),
                displayOrder
        );

        Pot savedPot = potRepository.save(pot);

        return PotResponse.from(savedPot);
    }

    public List<PotResponse> getAll(
            Long userId,
            boolean includeArchived
    ) {
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

        return pots.stream()
                .map(PotResponse::from)
                .toList();
    }

    public PotResponse getOne(
            Long userId,
            Long potId
    ) {
        return PotResponse.from(getOwnedPot(userId, potId));
    }

    @Transactional
    public PotResponse update(
            Long userId,
            Long potId,
            PotUpdateRequest request
    ) {
        Pot pot = getOwnedPot(userId, potId);

        pot.update(
                request.name(),
                request.goalCategory() == null
                        ? null
                        : normalizeNullableText(request.goalCategory()),
                request.targetAmount(),
                request.monthlyAllocation(),
                request.displayOrder()
        );

        return PotResponse.from(pot);
    }

    @Transactional
    public PotResponse updateArchived(
            Long userId,
            Long potId,
            PotArchiveRequest request
    ) {
        Pot pot = getOwnedPot(userId, potId);

        pot.updateArchived(request.archived());

        return PotResponse.from(pot);
    }

    public Pot getOwnedPot(
            Long userId,
            Long potId
    ) {
        return potRepository.findByIdAndUser_Id(potId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pot을 찾을 수 없습니다."
                ));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "사용자를 찾을 수 없습니다."
                ));
    }

    private String normalizeNullableText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}

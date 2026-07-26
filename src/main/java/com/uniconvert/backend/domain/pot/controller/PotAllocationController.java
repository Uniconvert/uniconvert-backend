package com.uniconvert.backend.domain.pot.controller;

import com.uniconvert.backend.domain.pot.dto.request.PotAllocationUpsertRequest;
import com.uniconvert.backend.domain.pot.dto.response.PotAllocationResponse;
import com.uniconvert.backend.domain.pot.service.PotAllocationService;
import com.uniconvert.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(
        name = "Pot Allocations",
        description = "Pot 월별 배정 이력 API"
)
public class PotAllocationController {

    private final PotAllocationService allocationService;

    public PotAllocationController(
            PotAllocationService allocationService
    ) {
        this.allocationService = allocationService;
    }

    @PostMapping("/pots/{potId}/allocations")
    @Operation(summary = "Pot 월별 금액 배정 | 텐텐")
    public ResponseEntity<PotAllocationResponse> upsert(
            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            @PathVariable
            Long potId,

            @Valid
            @RequestBody
            PotAllocationUpsertRequest request
    ) {
        return ResponseEntity.ok(
                allocationService.upsert(
                        userDetails.getUserId(),
                        potId,
                        request
                )
        );
    }

    @GetMapping("/pots/{potId}/allocations")
    @Operation(summary = "특정 Pot 배정 이력 조회 | 텐텐")
    public ResponseEntity<List<PotAllocationResponse>> getByPot(
            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            @PathVariable
            Long potId
    ) {
        return ResponseEntity.ok(
                allocationService.getByPot(
                        userDetails.getUserId(),
                        potId
                )
        );
    }

    @GetMapping("/pot-allocations")
    @Operation(summary = "월별 Pot 배정 내역 조회 | 텐텐")
    public ResponseEntity<List<PotAllocationResponse>> getByMonth(
            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            @RequestParam
            String yearMonth
    ) {
        return ResponseEntity.ok(
                allocationService.getByMonth(
                        userDetails.getUserId(),
                        yearMonth
                )
        );
    }
}

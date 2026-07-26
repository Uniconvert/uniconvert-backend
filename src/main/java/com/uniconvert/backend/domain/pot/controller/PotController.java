package com.uniconvert.backend.domain.pot.controller;

import com.uniconvert.backend.domain.pot.dto.request.PotArchiveRequest;
import com.uniconvert.backend.domain.pot.dto.request.PotCreateRequest;
import com.uniconvert.backend.domain.pot.dto.request.PotUpdateRequest;
import com.uniconvert.backend.domain.pot.dto.response.PotResponse;
import com.uniconvert.backend.domain.pot.service.PotService;
import com.uniconvert.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pots")
@Tag(name = "Pots", description = "목표 저축 Pot API")
public class PotController {

    private final PotService potService;

    public PotController(PotService potService) {
        this.potService = potService;
    }

    @PostMapping
    @Operation(summary = "Pot 생성 | 텐텐")
    public ResponseEntity<PotResponse> create(
            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            @Valid
            @RequestBody
            PotCreateRequest request
    ) {
        PotResponse response = potService.create(
                userDetails.getUserId(),
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    @Operation(summary = "Pot 목록 조회 | 텐텐")
    public ResponseEntity<List<PotResponse>> getAll(
            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            @RequestParam(defaultValue = "false")
            boolean includeArchived
    ) {
        return ResponseEntity.ok(
                potService.getAll(
                        userDetails.getUserId(),
                        includeArchived
                )
        );
    }

    @GetMapping("/{potId}")
    @Operation(summary = "Pot 상세 조회 | 텐텐")
    public ResponseEntity<PotResponse> getOne(
            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            @PathVariable
            Long potId
    ) {
        return ResponseEntity.ok(
                potService.getOne(
                        userDetails.getUserId(),
                        potId
                )
        );
    }

    @PatchMapping("/{potId}")
    @Operation(summary = "Pot 수정 | 텐텐")
    public ResponseEntity<PotResponse> update(
            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            @PathVariable
            Long potId,

            @Valid
            @RequestBody
            PotUpdateRequest request
    ) {
        return ResponseEntity.ok(
                potService.update(
                        userDetails.getUserId(),
                        potId,
                        request
                )
        );
    }

    @PatchMapping("/{potId}/archive")
    @Operation(summary = "Pot 보관 상태 변경 | 텐텐")
    public ResponseEntity<PotResponse> updateArchived(
            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            @PathVariable
            Long potId,

            @RequestBody
            PotArchiveRequest request
    ) {
        return ResponseEntity.ok(
                potService.updateArchived(
                        userDetails.getUserId(),
                        potId,
                        request
                )
        );
    }
}
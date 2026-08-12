package com.uniconvert.backend.domain.pot.controller;

import com.uniconvert.backend.domain.pot.dto.request.PotArchiveRequest;
import com.uniconvert.backend.domain.pot.dto.request.PotCreateRequest;
import com.uniconvert.backend.domain.pot.dto.request.PotUpdateRequest;
import com.uniconvert.backend.domain.pot.dto.response.PotResponse;
import com.uniconvert.backend.domain.pot.service.PotService;
import com.uniconvert.backend.global.security.CustomUserDetails;
import com.uniconvert.backend.domain.pot.dto.response.PotDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pots")
@Tag(
        name = "Pots",
        description = """
                목표 저축 Pot 생성·조회·수정·보관 API입니다.

                모든 API는 로그인 후 발급받은 Access Token이 필요합니다.
                Swagger 오른쪽 위 Authorize 버튼에 Access Token을 입력한 뒤 사용합니다.
                """
)
@SecurityRequirement(name = "bearerAuth")
public class PotController {

    private final PotService potService;

    public PotController(PotService potService) {
        this.potService = potService;
    }

    @PostMapping
    @Operation(
            summary = "Pot 생성 | 텐텐",
            description = """
                    새로운 목표 저축 Pot을 생성합니다.

                    입력값 안내:
                    - name: Pot 이름
                    - goalCategory: 대표 목표 카테고리
                    - targetAmount: 최종 목표 금액
                    - monthlyAllocation: 매월 저축할 계획 금액
                    - displayOrder: 화면에 표시할 순서

                    요청 예시:
                    {
                      "name": "유럽 여행",
                      "goalCategory": "TRAVEL",
                      "targetAmount": 3000000,
                      "monthlyAllocation": 300000,
                      "displayOrder": 1
                    }

                    생성 직후:
                    - monthlyAllocation 금액이 현재 월 실제 배정 금액으로 자동 등록됩니다.
                    - savedAmount에 최초 배정 금액이 반영됩니다.
                    - thisMonthAmount에는 현재 월 배정 금액이 반환됩니다.
                    - 해당 금액은 총 보유자산(remaining-budget) 계산에서 자동 차감됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Pot 생성 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청값 검증 실패"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 토큰이 없거나 유효하지 않음"
            )
    })
    public ResponseEntity<PotResponse> create(
            @Parameter(hidden = true)
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
    @Operation(
            summary = "Pot 목록 조회 | 텐텐",
            description = """
                    로그인한 사용자의 Pot 목록을 표시 순서대로 조회합니다.

                    응답 금액 필드:
                    - targetAmount: 최종 목표 금액
                    - savedAmount: 모든 월의 실제 누적 배정 금액
                    - monthlyAllocation: 매월 저축할 계획 금액
                    - thisMonthAmount: 사용자 시간대 기준 이번 달 실제 배정 금액

                    includeArchived 사용법:
                    - false: 보관되지 않은 Pot만 조회
                    - true: 보관된 Pot을 포함하여 전체 조회

                    호출 예시:
                    GET /pots?includeArchived=false
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pot 목록 조회 성공"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 토큰이 없거나 유효하지 않음"
            )
    })
    public ResponseEntity<List<PotResponse>> getAll(
            @Parameter(hidden = true)
            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            @Parameter(
                    description = "보관된 Pot 포함 여부",
                    example = "false"
            )
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
    @Operation(
            summary = "Pot 상세 조회 | 텐텐",
            description = """
                    지정한 Pot의 상세 정보를 조회합니다.

                    응답 금액 필드:
                    - targetAmount: 최종 목표 금액
                    - savedAmount: 모든 월의 실제 누적 배정 금액
                    - monthlyAllocation: 매월 저축할 계획 금액
                    - thisMonthAmount: 사용자 시간대 기준 이번 달 실제 배정 금액

                    호출 예시:
                    GET /pots/1
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pot 상세 조회 성공"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 토큰이 없거나 유효하지 않음"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pot을 찾을 수 없거나 다른 사용자의 Pot임"
            )
    })
    public ResponseEntity<PotDetailResponse> getOne(
            @Parameter(hidden = true)
            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            @Parameter(
                    description = "조회할 Pot ID",
                    example = "1"
            )
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
    @Operation(
            summary = "Pot 수정 | 텐텐",
            description = """
                    지정한 Pot의 이름, 카테고리, 목표 금액,
                    월 계획 금액 또는 표시 순서를 수정합니다.

                    요청 예시:
                    {
                      "name": "유럽 여행 경비",
                      "goalCategory": "TRAVEL",
                      "targetAmount": 3500000,
                      "monthlyAllocation": 350000,
                      "displayOrder": 1
                    }

                    요청에서 null로 전달되거나 생략된 값의 처리 방식은
                    PotUpdateRequest와 PotService의 수정 정책을 따릅니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pot 수정 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청값 검증 실패"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 토큰이 없거나 유효하지 않음"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pot을 찾을 수 없거나 다른 사용자의 Pot임"
            )
    })
    public ResponseEntity<PotResponse> update(
            @Parameter(hidden = true)
            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            @Parameter(
                    description = "수정할 Pot ID",
                    example = "1"
            )
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
    @Operation(
            summary = "Pot 보관 상태 변경 | 텐텐",
            description = """
                    지정한 Pot의 보관 상태를 변경합니다.

                    요청 예시:
                    {
                      "archived": true
                    }

                    - archived=true: Pot을 보관 처리합니다.
                    - archived=false: 보관된 Pot을 다시 활성화합니다.
                    - 보관된 Pot은 기본 GET /pots 조회에서 제외됩니다.
                    - GET /pots?includeArchived=true로 조회하면 보관된 Pot도 확인할 수 있습니다.

                    이 API는 Pot을 실제 삭제하지 않습니다.
                    Pot 및 월별 배정 데이터는 DB에 유지됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pot 보관 상태 변경 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청값 검증 실패"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 토큰이 없거나 유효하지 않음"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pot을 찾을 수 없거나 다른 사용자의 Pot임"
            )
    })
    public ResponseEntity<PotResponse> updateArchived(
            @Parameter(hidden = true)
            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            @Parameter(
                    description = "보관 상태를 변경할 Pot ID",
                    example = "1"
            )
            @PathVariable
            Long potId,

            @Valid
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
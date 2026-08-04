package com.uniconvert.backend.domain.pot.controller;

import com.uniconvert.backend.domain.pot.dto.request.PotAllocationUpsertRequest;
import com.uniconvert.backend.domain.pot.dto.response.PotAllocationResponse;
import com.uniconvert.backend.domain.pot.service.PotAllocationService;
import com.uniconvert.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(
        name = "Pot Allocations",
        description = """
                Pot에 실제로 배정한 금액을 월별로 관리하는 API입니다.

                모든 API는 로그인 후 발급받은 Access Token이 필요합니다.
                Swagger 오른쪽 위 Authorize 버튼에 Access Token을 입력한 뒤 사용합니다.

                yearMonth는 YYYY-MM 형식으로 입력합니다.
                예: 2026-08
                """
)
@SecurityRequirement(name = "bearerAuth")
public class PotAllocationController {

    private final PotAllocationService allocationService;

    public PotAllocationController(
            PotAllocationService allocationService
    ) {
        this.allocationService = allocationService;
    }

    @PostMapping("/pots/{potId}/allocations")
    @Operation(
            summary = "Pot 월별 금액 배정 | 텐텐",
            description = """
                    지정한 Pot에 특정 월의 실제 배정 금액을 저장합니다.

                    요청 예시:
                    {
                      "yearMonth": "2026-08",
                      "amount": 300000
                    }

                    처리 방식:
                    - 해당 Pot과 yearMonth 조합의 배정 내역이 없으면 새로 생성합니다.
                    - 같은 Pot과 yearMonth의 내역이 이미 있으면 기존 금액을 수정합니다.
                    - 기존 금액에 추가로 더하는 방식이 아니라, 전달한 amount로 변경됩니다.
                    - 수정된 금액 차이만큼 Pot의 savedAmount도 함께 변경됩니다.

                    예시:
                    - 기존 2026-08 배정 금액: 200000원
                    - 요청 amount: 300000원
                    - 실제 증가 금액: 100000원
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "월별 Pot 배정 금액 저장 또는 수정 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "금액 또는 yearMonth 형식이 올바르지 않거나 보관된 Pot임"
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
    public ResponseEntity<PotAllocationResponse> upsert(
            @Parameter(hidden = true)
            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            @Parameter(
                    description = "금액을 배정할 Pot ID",
                    example = "1"
            )
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
    @Operation(
            summary = "특정 Pot 배정 이력 조회 | 텐텐",
            description = """
                    지정한 Pot의 전체 월별 배정 이력을 조회합니다.

                    호출 예시:
                    GET /pots/1/allocations

                    응답 예시:
                    [
                      {
                        "allocationId": 3,
                        "potId": 1,
                        "potName": "유럽 여행",
                        "yearMonth": "2026-08",
                        "amount": 300000
                      },
                      {
                        "allocationId": 2,
                        "potId": 1,
                        "potName": "유럽 여행",
                        "yearMonth": "2026-07",
                        "amount": 250000
                      }
                    ]

                    배정 이력은 최근 yearMonth부터 조회됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "특정 Pot의 월별 배정 이력 조회 성공"
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
    public ResponseEntity<List<PotAllocationResponse>> getByPot(
            @Parameter(hidden = true)
            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            @Parameter(
                    description = "배정 이력을 조회할 Pot ID",
                    example = "1"
            )
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
    @Operation(
            summary = "월별 Pot 배정 내역 조회 | 텐텐",
            description = """
                    로그인한 사용자의 특정 월 전체 Pot 배정 내역을 조회합니다.

                    호출 예시:
                    GET /pot-allocations?yearMonth=2026-08

                    활용 예시:
                    - 2026년 8월에 각 Pot에 배정한 금액 확인
                    - 이번 달 전체 Pot 배정 내역 표시
                    - 이번 달 Pot별 thisMonthAmount 확인

                    이 API는 개별 Pot 한 개가 아니라,
                    해당 월에 배정 내역이 존재하는 사용자의 모든 Pot을 조회합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "해당 월 전체 Pot 배정 내역 조회 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "yearMonth 형식이 올바르지 않음"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 토큰이 없거나 유효하지 않음"
            )
    })
    public ResponseEntity<List<PotAllocationResponse>> getByMonth(
            @Parameter(hidden = true)
            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            @Parameter(
                    description = "조회할 월. YYYY-MM 형식으로 입력",
                    example = "2026-08",
                    required = true
            )
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
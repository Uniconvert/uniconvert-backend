package com.uniconvert.backend.global.exception;

import org.springframework.http.HttpStatus;

import java.util.Locale;

public enum ErrorCode {
    SUCCESS(HttpStatus.OK, "SUCCESS", "요청이 성공했습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "잘못된 요청입니다."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "입력값 검증에 실패했습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "요청한 리소스를 찾을 수 없습니다."),
    CONFLICT(HttpStatus.CONFLICT, "CONFLICT", "요청이 현재 상태와 충돌합니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "TOKEN_INVALID", "토큰이 유효하지 않습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "토큰이 만료되었습니다."),
    LOGIN_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "LOGIN_INVALID_CREDENTIALS", "비밀번호를 잘못 입력했어요. 비밀번호를 재설정해 주세요"),
    LOGIN_PASSWORD_FAILURE_LIMIT(HttpStatus.UNAUTHORIZED, "LOGIN_PASSWORD_FAILURE_LIMIT", "비밀번호가 계속 일치하지 않아요. 계정을 다시 확인하시거나 회원가입을 다시 진행해 주세요"),
    EMAIL_ALREADY_REGISTERED(HttpStatus.CONFLICT, "EMAIL_ALREADY_REGISTERED", "이미 가입된 이메일이에요. 로그인해 주세요"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."),
    EXPENSE_NOT_FOUND(HttpStatus.NOT_FOUND, "EXPENSE_NOT_FOUND", "지출 내역을 찾을 수 없습니다."),
    MEMO_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMO_NOT_FOUND", "삭제할 메모가 존재하지 않거나 이미 삭제되었습니다."),
    INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "INVALID_CATEGORY", "유효하지 않은 카테고리입니다."),
    BUDGET_NOT_FOUND(HttpStatus.NOT_FOUND, "BUDGET_NOT_FOUND", "해당 월의 예산을 찾을 수 없습니다."),
    EMPTY_FILE(HttpStatus.BAD_REQUEST, "EMPTY_FILE", "업로드한 파일이 비어 있습니다."),
    INVALID_CSV_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_CSV_FORMAT", "지원하지 않는 CSV 형식입니다. (Wise 또는 Monzo 명세서만 지원합니다)"),
    UNSUPPORTED_CURRENCY(HttpStatus.BAD_REQUEST, "UNSUPPORTED_CURRENCY", "지원하지 않는 통화입니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getMessageKey() {
        return "error." + code.toLowerCase(Locale.ROOT);
    }
}

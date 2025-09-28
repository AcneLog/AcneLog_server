package hongik.triple.apimodule.global.common;

import hongik.triple.commonmodule.exception.ErrorCode;

import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime timestamp,
        Integer code,
        String message,
        String detail) {

    public ErrorResponse(ErrorCode errorcode) {
        this(LocalDateTime.now(), errorcode.getCode(), errorcode.getMessage(), "");
    }

    public ErrorResponse(String message) {
        this(LocalDateTime.now(), ErrorCode.INTERNAL_SERVER_EXCEPTION.getCode(), ErrorCode.INTERNAL_SERVER_EXCEPTION.getMessage(), message);
    }

    public ErrorResponse(ErrorCode errorcode, String message) {
        this(LocalDateTime.now(), errorcode.getCode(), errorcode.getMessage(), message);
    }
}

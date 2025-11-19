package hongik.triple.commonmodule.exception;

import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
public enum ErrorCode {

    // 1000: Success Case
    SUCCESS(HttpStatus.OK, 1000, "정상적인 요청입니다."),
    CREATED(HttpStatus.CREATED, 1001, "정상적으로 생성되었습니다."),

    // 2000: Common Error
    INTERNAL_SERVER_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, 2000, "예기치 못한 오류가 발생했습니다."),
    NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND, 2001, "존재하지 않는 리소스입니다."),
    INVALID_VALUE_EXCEPTION(HttpStatus.BAD_REQUEST, 2002, "올바르지 않은 요청 값입니다."),
    UNAUTHORIZED_EXCEPTION(HttpStatus.UNAUTHORIZED, 2003, "권한이 없는 요청입니다."),
    ALREADY_DELETE_EXCEPTION(HttpStatus.BAD_REQUEST, 2004, "이미 삭제된 리소스입니다."),
    FORBIDDEN_EXCEPTION(HttpStatus.FORBIDDEN, 2005, "인가되지 않는 요청입니다."),
    ALREADY_EXIST_EXCEPTION(HttpStatus.BAD_REQUEST, 2006, "이미 존재하는 리소스입니다."),
    INVALID_SORT_EXCEPTION(HttpStatus.BAD_REQUEST, 2007, "올바르지 않은 정렬 값입니다."),

    // 3000: Image Error
    EMPTY_FILE_EXCEPTION(HttpStatus.BAD_REQUEST, 3000, "파일이 비어있습니다."),
    INVALID_FILENAME_EXCEPTION(HttpStatus.BAD_REQUEST, 3001, "파일 이름이 유효하지 않습니다."),
    FILE_IO_EXCEPTION(HttpStatus.BAD_REQUEST, 3002, "파일 입출력 처리 중 예상치 못한 오류가 발생했습니다."),
    FAILED_UPLOAD_FILE(HttpStatus.INTERNAL_SERVER_ERROR, 3003, "파일 업로드에 실패하였습니다."),
    EMPTY_S3_KEY_EXCEPTION(HttpStatus.BAD_REQUEST, 3004, "S3 key 값이 비어있습니다."),
    NOT_FOUND_S3_EXCEPTION(HttpStatus.NOT_FOUND, 3005, "존재하지 않는 S3 객체입니다."),
    FAILED_DELETE_FILE(HttpStatus.INTERNAL_SERVER_ERROR, 3006, "이미지 삭제에 실패하였습니다."),
    NOT_ALLOWED_FILE_EXTENSION(HttpStatus.BAD_REQUEST, 3007, "올바르지 않은 파일 확장자입니다.");

    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, Integer code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
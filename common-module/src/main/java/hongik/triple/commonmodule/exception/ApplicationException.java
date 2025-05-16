package hongik.triple.commonmodule.exception;

import lombok.Getter;

@Getter
public class ApplicationException extends RuntimeException{
    private final ErrorCode errorCode;

    public ApplicationException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}

package tgb.cryptoexchange.detailsapi.exceptions;

import lombok.Getter;
import tgb.cryptoexchange.detailsapi.enums.ErrorCode;

@Getter
public class BaseException extends RuntimeException implements CustomException {

    private final ErrorCode errorCode;

    private final String field;

    private final String description;

    public BaseException(String message) {
        super(message);
        this.errorCode = ErrorCode.INTERNAL;
        this.field = null;
        this.description = null;
    }

}

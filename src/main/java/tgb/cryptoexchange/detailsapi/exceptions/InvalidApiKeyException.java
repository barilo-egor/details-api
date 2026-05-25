package tgb.cryptoexchange.detailsapi.exceptions;

import lombok.Getter;
import tgb.cryptoexchange.detailsapi.enums.ErrorCode;

@Getter
public class InvalidApiKeyException extends RuntimeException implements CustomException {

    private final ErrorCode errorCode;

    private final String field;

    private final String description;

    public InvalidApiKeyException() {
        super("Client not found.");
        this.field = "apiKey";
        this.errorCode = ErrorCode.INVALID_ARGUMENT;
        this.description = "ApiKey is invalid.";
    }

}

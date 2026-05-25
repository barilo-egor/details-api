package tgb.cryptoexchange.detailsapi.exceptions;

import lombok.Getter;
import tgb.cryptoexchange.detailsapi.enums.ErrorCode;

@Getter
public class ClientNotFoundException extends RuntimeException implements CustomException {

    private final ErrorCode errorCode;

    private final String field;

    private final String description;

    public ClientNotFoundException() {
        super("Client not found.");
        this.errorCode = ErrorCode.NOT_FOUND;
        this.field = null;
        this.description = null;
    }

}

package tgb.cryptoexchange.detailsapi.exceptions;

import tgb.cryptoexchange.detailsapi.enums.ErrorCode;

public interface CustomException {

    ErrorCode getErrorCode();

    String getField();

    String getDescription();

}

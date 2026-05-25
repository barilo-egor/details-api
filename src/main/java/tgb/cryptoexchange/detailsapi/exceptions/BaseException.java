package tgb.cryptoexchange.detailsapi.exceptions;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

    public BaseException(String message) {
        super(message);
    }

}

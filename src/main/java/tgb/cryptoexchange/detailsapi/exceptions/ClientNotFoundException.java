package tgb.cryptoexchange.detailsapi.exceptions;

import lombok.Getter;

@Getter
public class ClientNotFoundException extends RuntimeException {

    public ClientNotFoundException() {
        super("Client not found.");
    }

}

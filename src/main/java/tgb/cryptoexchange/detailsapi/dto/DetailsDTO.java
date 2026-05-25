package tgb.cryptoexchange.detailsapi.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DetailsDTO {

    private String requestMethod;

    private String details;

    private String bank;

    private String operator;

}

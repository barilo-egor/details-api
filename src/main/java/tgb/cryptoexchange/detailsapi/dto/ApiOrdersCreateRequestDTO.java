package tgb.cryptoexchange.detailsapi.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiOrdersCreateRequestDTO {

    private Long clientId;

    private String internalId;

    private String merchant;

    private String merchantOrderId;

    private String merchantOrderStatus;

    private Integer amount;

    private boolean enableUniqueAmount;

    private String callbackUrl;

}

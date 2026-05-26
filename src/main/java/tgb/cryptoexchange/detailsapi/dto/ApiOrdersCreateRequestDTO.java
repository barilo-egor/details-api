package tgb.cryptoexchange.detailsapi.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ApiOrdersCreateRequestDTO {

    private UUID id;

    private Long clientId;

    private String internalId;

    private String merchant;

    private String merchantOrderId;

    private String merchantOrderStatus;

    private Integer amount;

    private boolean enableUniqueAmount;

    private String callbackUrl;

}

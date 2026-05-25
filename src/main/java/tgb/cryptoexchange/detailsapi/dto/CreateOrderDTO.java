package tgb.cryptoexchange.detailsapi.dto;

import lombok.Builder;
import lombok.Data;
import tgb.cryptoexchange.detailsapi.enums.RequestMethod;

import java.util.Set;

@Data
@Builder
public class CreateOrderDTO {

    private String internalId;

    private Integer amount;

    private Set<RequestMethod> methods;

    @Builder.Default
    private boolean enableUniqueAmount = false;

    private String callback;

    private String userId;

}

package tgb.cryptoexchange.detailsapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;
import tgb.cryptoexchange.detailsapi.enums.RequestMethod;

import java.util.Set;

@Data
@Builder
public class CreateOrderDTO {

    @NotBlank(message = "Обязательно для заполнения")
    private String internalId;

    @NotNull(message = "Обязательно для заполнения")
    @Positive(message = "Сумма должна быть больше нуля")
    private Integer amount;

    private Set<RequestMethod> methods;

    @Builder.Default
    private boolean enableUniqueAmount = false;

    private String callbackUrl;

    private String userId;

}

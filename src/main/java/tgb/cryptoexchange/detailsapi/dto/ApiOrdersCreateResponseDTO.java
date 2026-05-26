package tgb.cryptoexchange.detailsapi.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ApiOrdersCreateResponseDTO {

    private UUID id;

    private Long clientId;

    private String internalId;

    private String status;

    private Integer amount;

    private boolean enableUniqueAmount;

    private String callbackUrl;

    private Instant createdAt;

    private Instant expiresAt;

}

package tgb.cryptoexchange.detailsapi.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tgb.cryptoexchange.detailsapi.dto.ApiDetailsResponseDTO;
import tgb.cryptoexchange.detailsapi.dto.ApiOrdersCreateRequestDTO;
import tgb.cryptoexchange.detailsapi.dto.ApiOrdersCreateResponseDTO;
import tgb.cryptoexchange.detailsapi.dto.CreateOrderDTO;
import tgb.cryptoexchange.detailsapi.exceptions.EnableUniqueAmountException;
import tgb.cryptoexchange.grpc.generated.CreateOrderGrpc;
import tgb.cryptoexchange.grpc.generated.CreateOrderResponseGrpc;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
public class OrdersMapper {

    public ApiOrdersCreateRequestDTO createRequestDTO(UUID orderId, CreateOrderDTO clientRequest,
            ApiDetailsResponseDTO detailsResponseDTO) {
        Integer amount;
        if (clientRequest.isEnableUniqueAmount()) {
            amount = Objects.isNull(detailsResponseDTO.getAmount()) ?
                    clientRequest.getAmount() :
                    detailsResponseDTO.getAmount();
        } else {
            if (Objects.nonNull(detailsResponseDTO.getAmount())) {
                throw new EnableUniqueAmountException();
            }
            amount = clientRequest.getAmount();
        }

        return ApiOrdersCreateRequestDTO.builder()
                .id(orderId)
                .clientId(Long.valueOf(clientRequest.getUserId()))
                .internalId(clientRequest.getInternalId())
                .merchant(detailsResponseDTO.getMerchant())
                .merchantOrderId(detailsResponseDTO.getOrderId())
                .merchantOrderStatus(detailsResponseDTO.getOrderStatus())
                .amount(amount)
                .enableUniqueAmount(clientRequest.isEnableUniqueAmount())
                .callbackUrl(clientRequest.getCallbackUrl())
                .build();
    }

    public CreateOrderGrpc createOrderGrpc(ApiOrdersCreateRequestDTO createRequestDTO) {
        CreateOrderGrpc.Builder builder = CreateOrderGrpc.newBuilder()
                .setClientId(createRequestDTO.getClientId())
                .setInternalId(createRequestDTO.getInternalId())
                .setMerchant(createRequestDTO.getMerchant())
                .setMerchantOrderId(createRequestDTO.getMerchantOrderId())
                .setMerchantOrderStatus(createRequestDTO.getMerchantOrderStatus())
                .setAmount(createRequestDTO.getAmount())
                .setEnableUniqueAmount(createRequestDTO.isEnableUniqueAmount());
        if (createRequestDTO.getCallbackUrl() != null) {
            builder.setCallbackUrl(createRequestDTO.getCallbackUrl());
        }
        return builder.build();
    }

    public ApiOrdersCreateResponseDTO grpcResponseToDTO(CreateOrderResponseGrpc response) {
        return ApiOrdersCreateResponseDTO.builder()
                .id(UUID.fromString(response.getId()))
                .clientId(response.getClientId())
                .internalId(response.getInternalId())
                .status(response.getStatus())
                .amount(response.getAmount())
                .enableUniqueAmount(response.getEnableUniqueAmount())
                .callbackUrl(response.getCallbackUrl())
                .created_at(response.hasCreatedAt() ? Instant.ofEpochSecond(
                        response.getCreatedAt().getSeconds(),
                        response.getCreatedAt().getNanos()
                ) : null)
                .expires_at(response.hasExpiresAt() ? Instant.ofEpochSecond(
                        response.getExpiresAt().getSeconds(),
                        response.getExpiresAt().getNanos()
                ) : null)
                .build();
    }

}

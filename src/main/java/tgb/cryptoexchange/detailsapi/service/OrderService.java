package tgb.cryptoexchange.detailsapi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.detailsapi.dto.*;
import tgb.cryptoexchange.detailsapi.mapper.DetailsMapper;
import tgb.cryptoexchange.detailsapi.mapper.OrdersMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class OrderService {

    private final ApiMerchantDetailsGrpcService detailsGrpcService;

    private final DetailsMapper detailsMapper;

    private final OrdersMapper ordersMapper;

    private final ApiOrdersGrpcService apiOrdersGrpcService;

    public OrderService(ApiMerchantDetailsGrpcService detailsGrpcService, DetailsMapper detailsMapper,
            OrdersMapper ordersMapper, ApiOrdersGrpcService apiOrdersGrpcService) {
        this.detailsGrpcService = detailsGrpcService;
        this.detailsMapper = detailsMapper;
        this.ordersMapper = ordersMapper;
        this.apiOrdersGrpcService = apiOrdersGrpcService;
    }

    public OrderResponseDTO createOrder(CreateOrderDTO clientRequest, ClientByApiKeyDTO client,
            Integer clientOrderTimeout) {
        ApiDetailsRequestDTO apiDetailsRequestDTO = detailsMapper.orderToRequestDTO(clientRequest);
        UUID orderId = apiDetailsRequestDTO.getInternalId();

        ApiDetailsResponseDTO detailsResponseDTO = detailsGrpcService.getDetails(apiDetailsRequestDTO);
        log.debug("Для клиентского запроса {} найдены реквизиты в merchant-details {}", clientRequest,
                detailsResponseDTO);

        ApiOrdersCreateRequestDTO requestOrder = ordersMapper.createRequestDTO(orderId, clientRequest,
                detailsResponseDTO, client);
        ApiOrdersResponseDTO orderResponseDTO = apiOrdersGrpcService.createOrder(requestOrder);
        log.debug("Для клиентского запроса {} создан order в api-orders {}", clientRequest, orderResponseDTO);

        Instant expiresAt = orderResponseDTO.getCreatedAt().plusSeconds(clientOrderTimeout);
        OrderResponseDTO responseDTO = OrderResponseDTO.builder()
                .id(orderId)
                .internalId(clientRequest.getInternalId())
                .details(detailsResponseDTO.getDetails())
                .status(orderResponseDTO.getStatus())
                .createdAt(orderResponseDTO.getCreatedAt())
                .expiresAt(expiresAt)
                .build();
        log.debug("Для клиентского запроса {} сформирован ответ {}", clientRequest, responseDTO);
        return responseDTO;
    }

    public OrderResponseDTO testOrder(CreateOrderDTO clientRequest, Integer clientOrderTimeout) {
        Instant createdAt = Instant.now();
        Instant expiresAt = createdAt.plusSeconds(clientOrderTimeout);
        return OrderResponseDTO.builder()
                .id(UUID.randomUUID())
                .internalId(clientRequest.getInternalId())
                .details(DetailsDTO.builder()
                        .requestMethod("CARD")
                        .details("1111 2222 3333 4444")
                        .bank("ALFA")
                        .build())
                .status("NEW")
                .createdAt(createdAt)
                .expiresAt(expiresAt)
                .build();
    }

    public OrderResponseDTO findOrder(String id, Integer clientOrderTimeout, ClientByApiKeyDTO client) {
        ApiOrdersResponseDTO orderDTO = apiOrdersGrpcService.getOrders(id, client.getClientId());
        Instant createdAt = orderDTO.getCreatedAt();
        Instant expiresAt = createdAt.plusSeconds(clientOrderTimeout);
        return OrderResponseDTO.builder()
                .id(orderDTO.getId())
                .internalId(orderDTO.getInternalId())
                //                у ордера их нет, не описано до конца
                //                .details(orderDTO.get)
                .status(orderDTO.getStatus())
                .createdAt(createdAt)
                .expiresAt(expiresAt)
                .build();
    }

    public List<OrderResponseDTO> findOrders(Integer clientOrderTimeout, ClientByApiKeyDTO client, Pageable pageable) {
        List<ApiOrdersResponseDTO> orderDTO = apiOrdersGrpcService.findOrders(client.getClientId(), pageable);
        return orderDTO.stream().map(dto -> {
            Instant createdAt = dto.getCreatedAt();
            Instant expiresAt = createdAt.plusSeconds(clientOrderTimeout);
            return OrderResponseDTO.builder()
                    .id(dto.getId())
                    .internalId(dto.getInternalId())
                    //                у ордера их нет, не описано до конца
                    //                .details(orderDTO.get)
                    .status(dto.getStatus())
                    .createdAt(createdAt)
                    .expiresAt(expiresAt)
                    .build();
        }).toList();
    }

    public OrderResponseDTO cancelOrder(String id, Integer clientOrderTimeout, ClientByApiKeyDTO client) {
        apiOrdersGrpcService.cancelOrder(id, client.getClientId());
        return findOrder(id, clientOrderTimeout, client);
    }

}

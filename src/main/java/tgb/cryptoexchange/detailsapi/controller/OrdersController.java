package tgb.cryptoexchange.detailsapi.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import tgb.cryptoexchange.detailsapi.dto.ApiDetailsResponseDTO;
import tgb.cryptoexchange.detailsapi.dto.CreateOrderDTO;
import tgb.cryptoexchange.detailsapi.mapper.DetailsMapper;
import tgb.cryptoexchange.detailsapi.service.ApiMerchantDetailsGrpcService;

@Slf4j
@RestController
@RequestMapping("/orders")
public class OrdersController {

    private final ApiMerchantDetailsGrpcService detailsGrpcService;

    private final DetailsMapper detailsMapper;

    public OrdersController(ApiMerchantDetailsGrpcService detailsGrpcService, DetailsMapper detailsMapper) {
        this.detailsGrpcService = detailsGrpcService;
        this.detailsMapper = detailsMapper;
    }

    @PostMapping
    public void createOrder(@Valid @RequestBody CreateOrderDTO orderDTO,
            @RequestHeader(value = "X-Test-Order", required = false) String isTestOrder) {
        if (Boolean.parseBoolean(isTestOrder)) {
            log.info("Получен тестовый запрос (X-Test-Order = true).");

            return;
        }
        ApiDetailsResponseDTO detailsResponseDTO = detailsGrpcService.getDetails(
                detailsMapper.orderToRequestDTO(orderDTO));

    }

}

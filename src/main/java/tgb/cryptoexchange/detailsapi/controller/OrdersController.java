package tgb.cryptoexchange.detailsapi.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import tgb.cryptoexchange.detailsapi.dto.ClientByApiKeyDTO;
import tgb.cryptoexchange.detailsapi.dto.CreateOrderDTO;
import tgb.cryptoexchange.detailsapi.dto.OrderResponseDTO;
import tgb.cryptoexchange.detailsapi.service.OrderService;

@Slf4j
@RestController
@RequestMapping("/orders")
public class OrdersController {

    private final OrderService orderService;

    public OrdersController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderResponseDTO createOrder(@Valid @RequestBody CreateOrderDTO clientRequest,
            @RequestHeader(value = "X-Test-Order", required = false) String isTestOrder,
            @RequestHeader(value = "X-Order-Timeout") Integer clientOrderTimeout,
            @RequestAttribute("authenticatedClient") ClientByApiKeyDTO client) {
        if (Boolean.parseBoolean(isTestOrder)) {
            log.info("Получен тестовый запрос (X-Test-Order = true) для клиента {}.", client);
            return orderService.testOrder(clientRequest, clientOrderTimeout);
        }
        return orderService.createOrder(clientRequest, client, clientOrderTimeout);
    }

    @GetMapping("/{id}")
    public OrderResponseDTO getOrder(@PathVariable String id,
            @RequestHeader(value = "X-Order-Timeout") Integer clientOrderTimeout,
            @RequestAttribute("authenticatedClient") ClientByApiKeyDTO client) {
        return orderService.findOrderById(id, clientOrderTimeout, client);
    }

}

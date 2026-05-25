package tgb.cryptoexchange.detailsapi.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import tgb.cryptoexchange.detailsapi.dto.CreateOrderDTO;

@Slf4j
@RestController
@RequestMapping("/orders")
public class OrdersController {

    public OrdersController() {
    }

    @PostMapping
    public void createOrder(@RequestBody CreateOrderDTO orderDTO,
            @RequestHeader(value = "X-Test-Order", required = false) String isTestOrder) {
        if (Boolean.parseBoolean(isTestOrder)) {
            log.info("Получен тестовый запрос (X-Test-Order = true).");

            return;
        }

    }

}

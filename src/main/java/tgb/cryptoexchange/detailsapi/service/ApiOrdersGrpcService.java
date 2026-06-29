package tgb.cryptoexchange.detailsapi.service;

import com.google.common.util.concurrent.ListenableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.detailsapi.dto.ApiOrdersCreateRequestDTO;
import tgb.cryptoexchange.detailsapi.dto.ApiOrdersResponseDTO;
import tgb.cryptoexchange.detailsapi.exceptions.OrderNotFoundException;
import tgb.cryptoexchange.detailsapi.mapper.OrdersMapper;
import tgb.cryptoexchange.grpc.generated.*;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ApiOrdersGrpcService extends GrpcService {

    private final OrdersServiceGrpc.OrdersServiceFutureStub ordersFutureStub;

    private final OrdersMapper ordersMapper;

    public ApiOrdersGrpcService(OrdersMapper ordersMapper,
            OrdersServiceGrpc.OrdersServiceFutureStub ordersFutureStub) {
        this.ordersFutureStub = ordersFutureStub;
        this.ordersMapper = ordersMapper;
    }

    public ApiOrdersResponseDTO createOrder(ApiOrdersCreateRequestDTO createRequestDTO) {
        CreateOrderGrpc request = ordersMapper.createOrderGrpc(createRequestDTO);
        ListenableFuture<CreateOrderResponseGrpc> grpcFuture = ordersFutureStub.createOrder(request);
        CreateOrderResponseGrpc response = toCompletableFuture(grpcFuture).join();
        return ordersMapper.grpcResponseToDTO(response);
    }

    public ApiOrdersResponseDTO getOrders(String id, Long clientId) {
        return findByRequest(ordersMapper.getOrdersByIdGrpc(id, clientId))
                .or(() -> findByRequest(ordersMapper.getOrdersByExternalIdGrpc(id, clientId)))
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    public List<ApiOrdersResponseDTO> findOrders(Long clientId, Pageable pageable) {
        GetOrdersGrpc request = ordersMapper.getOrdersGrpc(clientId, pageable);
        ListenableFuture<GetOrdersResponseGrpc> grpcFuture = ordersFutureStub.getOrders(request);
        GetOrdersResponseGrpc response = toCompletableFuture(grpcFuture).join();
        return response.getOrdersList().stream().map(ordersMapper::getOrder).toList();
    }

    private Optional<ApiOrdersResponseDTO> findByRequest(GetOrdersGrpc request) {
        ListenableFuture<GetOrdersResponseGrpc> grpcFuture = ordersFutureStub.getOrders(request);
        GetOrdersResponseGrpc response = toCompletableFuture(grpcFuture).join();
        long total = response.getTotalElements();
        if (total > 0) {
            return response.getOrdersList().stream().map(ordersMapper::getOrder).findFirst();
        }
        return Optional.empty();
    }

}

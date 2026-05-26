package tgb.cryptoexchange.detailsapi.service;

import com.google.common.util.concurrent.ListenableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.detailsapi.dto.ApiOrdersCreateRequestDTO;
import tgb.cryptoexchange.detailsapi.dto.ApiOrdersCreateResponseDTO;
import tgb.cryptoexchange.detailsapi.mapper.OrdersMapper;
import tgb.cryptoexchange.grpc.generated.CreateOrderGrpc;
import tgb.cryptoexchange.grpc.generated.CreateOrderResponseGrpc;
import tgb.cryptoexchange.grpc.generated.OrdersServiceGrpc;

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

    public ApiOrdersCreateResponseDTO createOrder(ApiOrdersCreateRequestDTO createRequestDTO) {
        CreateOrderGrpc request = ordersMapper.createOrderGrpc(createRequestDTO);
        ListenableFuture<CreateOrderResponseGrpc> grpcFuture = ordersFutureStub.createOrder(request);
        CreateOrderResponseGrpc response = toCompletableFuture(grpcFuture).join();
        return ordersMapper.grpcResponseToDTO(response);
    }

}

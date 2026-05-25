package tgb.cryptoexchange.detailsapi.service;

import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.detailsapi.dto.ApiOrdersCreateRequestDTO;
import tgb.cryptoexchange.detailsapi.dto.ApiOrdersCreateResponseDTO;
import tgb.cryptoexchange.detailsapi.exceptions.BaseException;
import tgb.cryptoexchange.detailsapi.exceptions.MerchantDetailsNotFoundException;
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
        try {
            CreateOrderResponseGrpc response = toCompletableFuture(grpcFuture).join();
            return ordersMapper.grpcResponseToDTO(response);
        } catch (Exception ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            if (cause instanceof StatusRuntimeException statusEx) {
                com.google.rpc.Status status = io.grpc.protobuf.StatusProto.fromThrowable(statusEx);
                if (status != null && status.getCode() == com.google.rpc.Code.NOT_FOUND_VALUE) {
                    throw new MerchantDetailsNotFoundException("Реквизиты для api-сделки получены не были");
                }
                log.error("Системная gRPC ошибка от merchant-details: код={}", statusEx.getStatus().getCode());
                throw new BaseException("gRPC service error");
            }
            log.error("Непредвиденная ошибка сети при вызове gRPC", ex);
            throw new BaseException("System connection error");
        }
    }

}

package tgb.cryptoexchange.detailsapi.service;

import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.detailsapi.dto.ApiDetailsRequestDTO;
import tgb.cryptoexchange.detailsapi.dto.ApiDetailsResponseDTO;
import tgb.cryptoexchange.detailsapi.exceptions.BaseException;
import tgb.cryptoexchange.detailsapi.exceptions.MerchantDetailsNotFoundException;
import tgb.cryptoexchange.detailsapi.mapper.DetailsMapper;
import tgb.cryptoexchange.grpc.generated.GetDetailsGrpc;
import tgb.cryptoexchange.grpc.generated.GetDetailsResponseGrpc;
import tgb.cryptoexchange.grpc.generated.MerchantDetailsServiceGrpc;

@Service
@Slf4j
public class ApiMerchantDetailsGrpcService extends GrpcService {

    private final MerchantDetailsServiceGrpc.MerchantDetailsServiceFutureStub detailsFutureStub;

    private final DetailsMapper detailsMapper;

    public ApiMerchantDetailsGrpcService(DetailsMapper detailsMapper,
            MerchantDetailsServiceGrpc.MerchantDetailsServiceFutureStub detailsFutureStub) {
        this.detailsFutureStub = detailsFutureStub;
        this.detailsMapper = detailsMapper;
    }

    public ApiDetailsResponseDTO getDetails(ApiDetailsRequestDTO requestDTO) {
        GetDetailsGrpc request = detailsMapper.detailsRequestDTOToGrpc(requestDTO);
        ListenableFuture<GetDetailsResponseGrpc> grpcFuture = detailsFutureStub.getDetails(request);
        try {
            GetDetailsResponseGrpc response = toCompletableFuture(grpcFuture).join();
            return detailsMapper.grpcResponseToDTO(response);
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

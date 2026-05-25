package tgb.cryptoexchange.detailsapi.service;

import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.detailsapi.dto.ClientByApiKeyDTO;
import tgb.cryptoexchange.detailsapi.enums.ClientStatus;
import tgb.cryptoexchange.detailsapi.exceptions.BaseException;
import tgb.cryptoexchange.detailsapi.exceptions.ClientNotFoundException;
import tgb.cryptoexchange.detailsapi.exceptions.InvalidApiKeyException;
import tgb.cryptoexchange.grpc.generated.ClientsServiceGrpc;
import tgb.cryptoexchange.grpc.generated.GetClientByApiKeyGrpc;
import tgb.cryptoexchange.grpc.generated.GetClientByApiKeyResponseGrpc;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class ApiClientsGrpcService {

    private final ClientsServiceGrpc.ClientsServiceFutureStub clientsFutureStub;

    public ApiClientsGrpcService(ClientsServiceGrpc.ClientsServiceFutureStub clientsFutureStub) {
        this.clientsFutureStub = clientsFutureStub;
    }

    public ClientByApiKeyDTO getClientByApiKey(String keyHash) {
        GetClientByApiKeyGrpc request = GetClientByApiKeyGrpc.newBuilder()
                .setApiKey(keyHash)
                .build();

        ListenableFuture<GetClientByApiKeyResponseGrpc> grpcFuture = clientsFutureStub.getClientByApiKey(request);
        try {
            GetClientByApiKeyResponseGrpc response = toCompletableFuture(grpcFuture).join();
            return ClientByApiKeyDTO.builder()
                    .username(response.getUsername())
                    .secret(response.getSecret())
                    .status(ClientStatus.valueOf(response.getStatus())).build();
        } catch (Exception ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            if (cause instanceof StatusRuntimeException statusEx) {
                Status.Code code = statusEx.getStatus().getCode();
                if (code == Status.Code.NOT_FOUND) {
                    log.warn("Client not found via gRPC for hash: {}", keyHash);
                    throw new ClientNotFoundException();
                }
                if (code == Status.Code.INVALID_ARGUMENT) {
                    log.warn("Invalid key format sent to gRPC service: {}", statusEx.getMessage());
                    throw new InvalidApiKeyException();
                }
            }
            log.error("Error executing gRPC call", ex);
            throw new BaseException("Error executing gRPC call");
        }
    }

    /**
     * Утилитарный метод перевода Guava ListenableFuture в CompletableFuture
     */
    private <T> CompletableFuture<T> toCompletableFuture(ListenableFuture<T> listenableFuture) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        listenableFuture.addListener(() -> {
            try {
                completableFuture.complete(listenableFuture.get());
            } catch (Exception e) {
                completableFuture.completeExceptionally(e);
            }
        }, Runnable::run);
        return completableFuture;
    }

}

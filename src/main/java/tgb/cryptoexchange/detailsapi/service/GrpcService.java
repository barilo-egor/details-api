package tgb.cryptoexchange.detailsapi.service;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.CompletableFuture;

public abstract class GrpcService {

    /**
     * Утилитарный метод перевода Guava ListenableFuture в CompletableFuture
     */
    protected <T> CompletableFuture<T> toCompletableFuture(ListenableFuture<T> listenableFuture) {
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

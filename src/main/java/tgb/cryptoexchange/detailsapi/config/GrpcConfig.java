package tgb.cryptoexchange.detailsapi.config;

import io.grpc.Channel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;
import tgb.cryptoexchange.grpc.generated.ClientsServiceGrpc;
import tgb.cryptoexchange.grpc.generated.MerchantDetailsServiceGrpc;

@Configuration
public class GrpcConfig {

    @Bean
    public ClientsServiceGrpc.ClientsServiceFutureStub clientsServiceFutureStub(GrpcChannelFactory channelFactory) {
        Channel channel = channelFactory.createChannel("api-clients");
        return ClientsServiceGrpc.newFutureStub(channel);
    }

    @Bean
    public MerchantDetailsServiceGrpc.MerchantDetailsServiceFutureStub merchantDetailsServiceFutureStub(
            GrpcChannelFactory channelFactory) {
        Channel channel = channelFactory.createChannel("api-merchant-details");
        return MerchantDetailsServiceGrpc.newFutureStub(channel);
    }

    @Bean
    public MerchantDetailsServiceGrpc.MerchantDetailsServiceFutureStub merchantDetailsServiceFutureStub(
            GrpcChannelFactory channelFactory) {
        Channel channel = channelFactory.createChannel("api-merchant-details");
        return MerchantDetailsServiceGrpc.newFutureStub(channel);
    }



}

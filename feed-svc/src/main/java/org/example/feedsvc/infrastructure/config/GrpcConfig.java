package org.example.feedsvc.infrastructure.config;


import org.example.contentsvc.grpc.ContentServiceGrpc;
import org.example.socialsvc.grpc.SocialServiceGrpc;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.ImportGrpcClients;

@Configuration
@ImportGrpcClients(
        target = "social",
        types = {
                SocialServiceGrpc.SocialServiceBlockingStub.class
        }
)
@ImportGrpcClients(
        target = "content",
        types = {
                ContentServiceGrpc.ContentServiceBlockingStub.class
        }
)
public class GrpcConfig {
}

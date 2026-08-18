package org.example.feedsvc.infrastructure.config;

import org.example.contentsvc.grpc.ContentServiceGrpc;
import org.example.socialsvc.grpc.SocialServiceGrpc;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.ImportGrpcClients;


@ImportGrpcClients(
        target = "content",
        types = ContentServiceGrpc.ContentServiceBlockingStub.class
)
@ImportGrpcClients(
        target = "social",
        types = SocialServiceGrpc.SocialServiceBlockingStub.class
)
@Configuration
public class GrpcConfig {
}

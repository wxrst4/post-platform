package org.example.socialsvc.application.mapper;

import org.example.socialsvc.domain.entity.Subscription;
import org.example.socialsvc.presentation.subscription.dto.SubscriptionResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    SubscriptionResponse toResponse(Subscription subscription);

    List<SubscriptionResponse> toResponse(List<Subscription> subscriptions);
}

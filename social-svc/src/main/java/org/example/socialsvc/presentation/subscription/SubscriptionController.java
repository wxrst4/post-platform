package org.example.socialsvc.presentation.subscription;

import lombok.RequiredArgsConstructor;
import org.example.socialsvc.application.mapper.SubscriptionMapper;
import org.example.socialsvc.application.subscription.SubscriptionService;
import org.example.socialsvc.domain.entity.Subscription;
import org.example.socialsvc.presentation.subscription.dto.SubscriptionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final SubscriptionMapper subscriptionMapper;

    @PostMapping("/channels/{id}/subscribe")
    public ResponseEntity<SubscriptionResponse> subscribe(@PathVariable UUID id) {
        Subscription subscription = subscriptionService.subscribe(id);
        return ResponseEntity.ok(subscriptionMapper.toResponse(subscription));
    }

    @PostMapping("/channels/{id}/unsubscribe")
    public ResponseEntity<Void> unsubscribe(@PathVariable UUID id) {
        subscriptionService.unsubscribe(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<List<SubscriptionResponse>> getSubscriptions() {
        List<Subscription> subscriptions = subscriptionService.getMySubscriptions();
        return ResponseEntity.ok(subscriptionMapper.toResponse(subscriptions));
    }
}

package org.example.socialsvc.infrastructure.repository;

import org.example.socialsvc.domain.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    boolean existsByUserIdAndChannelId(UUID userId, UUID channelId);

    void deleteByUserIdAndChannelId(UUID userId, UUID channelId);

    List<Subscription> findAllByUserId(UUID userId);

    List<Subscription> findAllByChannelId(UUID channelId);
}

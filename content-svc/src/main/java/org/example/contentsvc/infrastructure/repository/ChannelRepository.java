package org.example.contentsvc.infrastructure.repository;

import org.example.contentsvc.domain.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, UUID> {

    boolean existsByName(String name);
}

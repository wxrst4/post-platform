package org.example.contentsvc.infrastructure.repository;

import org.example.contentsvc.domain.entity.PostMedia;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostMediaRepository extends CrudRepository<PostMedia, UUID> {

    List<PostMedia> findAllByPostIdOrderByCreatedAtAsc(UUID postId);
}

package org.example.contentsvc.application.mapper;

import org.example.contentsvc.domain.entity.Post;
import org.example.contentsvc.presentation.http.post.dto.PostResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PostMapper {

    PostResponse toResponse(Post entity);
    List<PostResponse> toResponse(List<Post> entities);
}

package org.example.socialsvc.application.mapper;

import org.example.socialsvc.domain.entity.Comment;
import org.example.socialsvc.presentation.comment.dto.CommentResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    CommentResponse toResponse(Comment comment);

    List<CommentResponse> toResponse(List<Comment> comments);
}

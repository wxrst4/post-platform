package org.example.usersvc.application.mapper;

import org.example.usersvc.domain.entity.UserEntity;
import org.example.usersvc.presentation.http.user.dto.CreateUserRequest;
import org.example.usersvc.presentation.http.user.dto.UserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserEntity toEntity(CreateUserRequest request);

    UserResponse toResponse(UserEntity entity);
}

package org.example.usersvc.application.mapper;

import org.example.usersvc.domain.entity.UserEntity;
import org.example.usersvc.presentation.http.auth.dto.RegisterUserRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    UserEntity toEntity(RegisterUserRequest request);
}

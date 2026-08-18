package org.example.usersvc.application.mapper;

import org.example.usersvc.domain.entity.RoleEntity;
import org.example.usersvc.presentation.http.role.dto.CreateRoleRequest;
import org.example.usersvc.presentation.http.role.dto.RoleResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleEntity toEntity(CreateRoleRequest request);

    RoleResponse toResponse(RoleEntity entity);
}

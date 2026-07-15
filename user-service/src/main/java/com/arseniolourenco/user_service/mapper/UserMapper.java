package com.arseniolourenco.user_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.arseniolourenco.user_service.dto.request.UserCreateRequest;
import com.arseniolourenco.user_service.dto.response.UserResponse;
import com.arseniolourenco.user_service.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toUser(UserCreateRequest request);

    UserResponse toUserResponse(User user);
}

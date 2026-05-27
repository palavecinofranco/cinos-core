package org.cinos.core.users.dto.mapper;

import org.cinos.core.users.dto.UserDTO;
import org.cinos.core.users.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toDTO(UserEntity userEntity);

    UserEntity toEntity(UserDTO userDTO);
}

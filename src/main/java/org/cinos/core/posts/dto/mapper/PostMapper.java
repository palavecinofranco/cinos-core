package org.cinos.core.posts.dto.mapper;

import org.cinos.core.posts.dto.PostDTO;
import org.cinos.core.posts.dto.PostLocationDTO;
import org.cinos.core.posts.entity.*;
import org.cinos.core.users.entity.AccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PostMapper {

    @Mapping(source = "images", target = "imagesUrls", qualifiedByName = "mapImages")
    @Mapping(source = "userAccount", target = "userFullName", qualifiedByName = "mapUserFullName")
    @Mapping(source = "userAccount", target = "userId", qualifiedByName = "mapUserId")
    @Mapping(source = "location", target = "location", qualifiedByName = "mapLocation")
    @Mapping(source = "userAccount", target = "userAvatar", qualifiedByName = "mapUserAvatar")
    @Mapping(source = "userAccount", target = "userPhone", qualifiedByName = "mapUserPhone")
    @Mapping(source = "userAccount", target = "userAttentionHours", qualifiedByName = "mapUserAttentionHours")
    @Mapping(source = "technicalVerification", target = "technicalVerification")
    @Mapping(source = "hp", target = "hp")
    @Mapping(source = "traccion", target = "traccion")
    @Mapping(source = "motor", target = "motor")
    @Mapping(source = "publicationDate", target = "publicationDate", qualifiedByName = "mapPublicationDate")
    PostDTO toDTO(PostEntity post);
    PostLocationDTO toLocationDTO(PostLocationEntity location);

    @Named("mapImages")
    default List<String> mapImages(List<PostImageEntity> images) {
        if (images == null) {
            return new ArrayList<>();
        }
        return images.stream()
                .map(PostImageEntity::getUrl)
                .toList();
    }

    @Named("mapUserFullName")
    default String mapUserFullName(AccountEntity account) {
        if (account == null || account.getUser() == null) {
            return null;
        }
        String firstName = account.getUser().getName() != null ? account.getUser().getName() : "";
        String lastName = account.getUser().getLastname() != null ? account.getUser().getLastname() : "";
        return (firstName + " " + lastName).trim();
    }

    @Named("mapUserId")
    default Long mapUserId(AccountEntity account) {
        if (account == null) {
            return null;
        }
        return account.getId();
    }

    @Named("mapLocation")
    default PostLocationDTO mapLocation(PostLocationEntity location) {
        return PostLocationDTO.builder()
                .address(location.getAddress())
                .lat(location.getLat())
                .lng(location.getLng())
                .build();
    }

    @Named("mapUserAvatar")
    default String mapUserAvatar(AccountEntity account) {
        return account.getAvatarImg();
    }

    @Named("mapUserPhone")
    default String mapUserPhone(AccountEntity account) {
        return account.getPhone();
    }

    @Named("mapUserAttentionHours")
    default String mapUserAttentionHours(AccountEntity account) {
        return account.getAttentionHours();
    }

    @Named("mapCarModel")
    default String mapCarModel(ModelEntity model) {
        return model.getName();
    }
    @Named("mapCarMake")
    default String mapCarMake(MakeEntity make) {
        return make.getName();
    }

    @Named("mapPublicationDate")
    default java.time.ZonedDateTime mapPublicationDate(java.time.LocalDateTime publicationDate) {
        if (publicationDate == null) {
            return null;
        }
        return publicationDate.atZone(ZoneId.systemDefault());
    }

}



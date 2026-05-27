package org.cinos.core.technical_verification.dto.mapper;

import org.cinos.core.technical_verification.dto.TechnicalVerificationDTO;
import org.cinos.core.technical_verification.entity.TechnicalVerification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TechnicalVerificationMapper {
    TechnicalVerificationDTO toDto(TechnicalVerification entity);
}
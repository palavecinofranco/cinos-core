package org.cinos.core.notifications.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cinos.core.notifications.entity.PushTokenEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenRegistrationRequest {

    private String token;
    private PushTokenEntity.DeviceType deviceType;
} 
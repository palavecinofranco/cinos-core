package org.cinos.core.notifications.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushNotificationRequest {

    private String title;
    private String body;
    private String imageUrl;
    private Map<String, String> data;
    private List<String> tokens;
    private List<Long> userIds;
    private NotificationType type;
    private boolean sendToPremiumOnly = false;

    public enum NotificationType {
        NEW_POST,
        VERIFICATION_COMPLETED,
        SUBSCRIPTION_UPDATE,
        SYSTEM_ANNOUNCEMENT,
        PREMIUM_FEATURE
    }
} 
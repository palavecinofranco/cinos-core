package org.cinos.core.notifications.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushNotificationResponse {

    private boolean success;
    private String message;
    private int totalTokens;
    private int successfulDeliveries;
    private int failedDeliveries;
    private List<String> failedTokens;
    private List<String> successfulTokens;
} 
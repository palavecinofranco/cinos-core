package org.cinos.core.users.controller.request;

import lombok.Data;

@Data
public class PremiumNotificationPreferencesRequest {
    private String brand;
    private String model;
    private String condition;
} 
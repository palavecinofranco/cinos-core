package org.cinos.core.users.dto;

import org.cinos.core.users.model.Role;

import java.util.List;

public record UserDTO(
        Long id,
String name,
String username,
String lastname,
String email,
String phone,
Boolean active,
List<Role> roles,
Boolean hasSeenRecommendationsModal,
String preferredBrand,
Boolean wantsUsedCars,
Boolean wantsNewCars,
Boolean useLocationForRecommendations,
    String premiumNotificationBrand,
    String premiumNotificationModel,
    String premiumNotificationCondition
) { }

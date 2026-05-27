package org.cinos.core.users.controller.request;

public record RecommendationsPreferencesRequest(
    String preferredBrand,
    Boolean wantsUsedCars,
    Boolean wantsNewCars,
    Boolean useLocationForRecommendations
) {} 
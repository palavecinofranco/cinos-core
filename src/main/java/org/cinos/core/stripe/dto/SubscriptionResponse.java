package org.cinos.core.stripe.dto;

import lombok.Builder;

@Builder
public record SubscriptionResponse(
        String clientSecret,
        String checkoutUrl,
        String message,
        Boolean success,
        String estado
) {
}
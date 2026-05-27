package org.cinos.core.stripe.dto;

import lombok.Data;

@Data
public class CreateSubscriptionRequest {
    private String planId;
}
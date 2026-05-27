package org.cinos.core.stripe.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionPlanDto {
    private String id;
    private String name;
    private int price; // en centavos
    private String currency;
    private String interval;
    private List<String> features;

}

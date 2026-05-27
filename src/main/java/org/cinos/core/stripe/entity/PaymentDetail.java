package org.cinos.core.stripe.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "PAYMENTS_DETAILS")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentDetail {
    @Id
    String id;
    String status;
    String customerId;
    String customerEmail;
    String subscriptionId;
    String paymentMethodId;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    Double price;


}

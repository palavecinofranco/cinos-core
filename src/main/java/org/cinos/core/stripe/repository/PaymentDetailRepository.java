package org.cinos.core.stripe.repository;

import org.cinos.core.stripe.entity.PaymentDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentDetailRepository extends JpaRepository<PaymentDetail, Long> {
}

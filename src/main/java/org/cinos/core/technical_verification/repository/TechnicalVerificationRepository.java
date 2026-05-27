package org.cinos.core.technical_verification.repository;

import org.cinos.core.technical_verification.entity.TechnicalVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TechnicalVerificationRepository extends JpaRepository<TechnicalVerification, Long> {
    Optional<TechnicalVerification> findByPost_Id(Long postId);
    // Cuenta las verificaciones técnicas enviadas por un usuario en el mes actual
    long countByPost_UserAccount_IdAndSentToVerificationDateBetween(Long accountId, java.time.LocalDateTime start, java.time.LocalDateTime end);
}

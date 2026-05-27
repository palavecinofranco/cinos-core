package org.cinos.core.users.repository;

import org.cinos.core.users.entity.PendingVerificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PendingVerificationRepository extends JpaRepository<PendingVerificationEntity, Long> {
    Optional<PendingVerificationEntity> findByEmail(String email);
}


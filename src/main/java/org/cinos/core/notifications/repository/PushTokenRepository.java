package org.cinos.core.notifications.repository;

import org.cinos.core.notifications.entity.PushTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PushTokenRepository extends JpaRepository<PushTokenEntity, Long> {

    Optional<PushTokenEntity> findByToken(String token);

    List<PushTokenEntity> findByUserIdAndIsActiveTrue(Long userId);

    @Query("SELECT pt FROM PushTokenEntity pt WHERE pt.user.id IN :userIds AND pt.isActive = true")
    List<PushTokenEntity> findByUserIdsAndActive(@Param("userIds") List<Long> userIds);

    @Query("SELECT pt FROM PushTokenEntity pt WHERE :premiumRole MEMBER OF pt.user.roles AND pt.isActive = true")
    List<PushTokenEntity> findActiveTokensForPremiumUsers(@Param("premiumRole") org.cinos.core.users.model.Role premiumRole);

    void deleteByToken(String token);

    boolean existsByToken(String token);
} 
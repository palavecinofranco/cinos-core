package org.cinos.core.users.repository;

import org.cinos.core.users.entity.UserEntity;
import org.cinos.core.users.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    <T> Optional<T> findById(Long id, Class<T> type);
    Optional<UserEntity> findByUsername(String username);
    <T> Optional<T> findByUsername(String username, Class<T> type);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByStripeCustomerId(String stripeCustomerId);
    java.util.Optional<UserEntity> findByStripeSubscriptionId(String stripeSubscriptionId);

    /**
     * Cuenta usuarios premium
     */
    @Query("SELECT COUNT(u) FROM UserEntity u WHERE :role MEMBER OF u.roles")
    long countByRolesContaining(@Param("role") Role role);

    /**
     * Cuenta usuarios con preferencias de notificaciones configuradas
     */
    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.premiumNotificationBrand IS NOT NULL OR u.premiumNotificationModel IS NOT NULL OR u.premiumNotificationCondition IS NOT NULL")
    long countByPremiumNotificationBrandIsNotNullOrPremiumNotificationModelIsNotNullOrPremiumNotificationConditionIsNotNull();

    /**
     * Encuentra usuarios premium que coinciden con las preferencias del post
     */
    @Query("SELECT u FROM UserEntity u WHERE :role MEMBER OF u.roles AND " +
           "((u.premiumNotificationBrand IS NULL OR u.premiumNotificationBrand = '' OR u.premiumNotificationBrand = :brand) AND " +
           "(u.premiumNotificationModel IS NULL OR u.premiumNotificationModel = '' OR u.premiumNotificationModel = :model) AND " +
           "(u.premiumNotificationCondition IS NULL OR u.premiumNotificationCondition = '' OR u.premiumNotificationCondition = :condition)) AND " +
           "(u.premiumNotificationBrand IS NOT NULL OR u.premiumNotificationModel IS NOT NULL OR u.premiumNotificationCondition IS NOT NULL)")
    List<UserEntity> findPremiumUsersMatchingPostPreferences(
        @Param("role") Role role,
        @Param("brand") String brand,
        @Param("model") String model,
        @Param("condition") String condition
    );

    /**
     * Encuentra todos los usuarios premium
     */
    @Query("SELECT u FROM UserEntity u WHERE :role MEMBER OF u.roles")
    List<UserEntity> findByRolesContaining(@Param("role") Role role);
}

package org.cinos.core.users.entity;

import org.cinos.core.users.model.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.cinos.core.posts.entity.PostEntity;

@Entity
@Getter
@Setter
@Table(name = "USERS")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity implements UserDetails {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String lastname;
    @Column(unique = true)
    private String username;
    @Column(unique = true)
    private String email;
    private String password;
    private String phone;
    private String address;
    private Boolean active;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    private List<Role> roles;

    private String verificationCode;
    private LocalDateTime verificationCodeExpiry;

    @Column(unique = true, nullable = true)
    private String stripeCustomerId;

    @Column(name = "stripe_subscription_id", unique = true, nullable = true)
    private String stripeSubscriptionId;

    @Column(name = "has_seen_recommendations_modal")
    private Boolean hasSeenRecommendationsModal = false;

    @Column(name = "preferred_brand")
    private String preferredBrand;

    @Column(name = "wants_used_cars")
    private Boolean wantsUsedCars;

    @Column(name = "wants_new_cars")
    private Boolean wantsNewCars;

    @Column(name = "use_location_for_recommendations")
    private Boolean useLocationForRecommendations;

    @Column(name = "premium_notification_brand")
    private String premiumNotificationBrand;

    @Column(name = "premium_notification_model")
    private String premiumNotificationModel;

    @Column(name = "premium_notification_condition")
    private String premiumNotificationCondition;

    @Column(name = "technical_verification_credits")
    private Integer technicalVerificationCredits = 0;

    @Column(name = "technical_verification_reports_credits")
    private Integer technicalVerificationReportsCredits = 0;

    @ManyToMany
    @JoinTable(
        name = "user_technical_verification_access",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "post_id")
    )
    private List<PostEntity> unlockedTechnicalVerifications = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (Role role : this.roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
        }
        return authorities;
    }
}

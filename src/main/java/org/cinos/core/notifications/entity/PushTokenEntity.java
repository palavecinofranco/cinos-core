package org.cinos.core.notifications.entity;

import jakarta.persistence.*;
import lombok.*;
import org.cinos.core.users.entity.UserEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "PUSH_TOKENS")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PushTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @Column(name = "device_type")
    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum DeviceType {
        ANDROID, IOS, WEB
    }
} 
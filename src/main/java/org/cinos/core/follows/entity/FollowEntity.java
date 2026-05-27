package org.cinos.core.follows.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "FOLLOWS")
public class FollowEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "from_user_id")
    private Long fromUserId;
    @Column(name = "to_user_id")
    private Long toUserId;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

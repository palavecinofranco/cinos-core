package org.cinos.core.users.entity;

import jakarta.persistence.*;
import lombok.*;
import org.cinos.core.posts.entity.PostEntity;

import java.util.List;

@Entity
@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
@Table(name = "ACCOUNTS")
public class AccountEntity {
    @Id
    private Long id;
    @OneToOne @MapsId @JoinColumn(name = "user_id")
    private UserEntity user;
    private Integer points;
    private String avatarImg;
    private Long followers;
    private Long followings;
    private Integer posts;
    @ManyToMany(mappedBy = "usersSaved")
    private List<PostEntity> savedPosts;
    private String phone;
    private String attentionHours;
}

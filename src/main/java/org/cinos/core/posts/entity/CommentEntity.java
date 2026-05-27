package org.cinos.core.posts.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "COMMENTS")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long postId;
    private Long userId;
    private String content;
    private LocalDateTime commentDate;

}

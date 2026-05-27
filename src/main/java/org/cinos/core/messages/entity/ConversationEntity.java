package org.cinos.core.messages.entity;

import jakarta.persistence.*;
import lombok.*;
import org.cinos.core.users.entity.AccountEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "CONVERSATIONS")
public class ConversationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany
    private Set<AccountEntity> participants = new HashSet<>();

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL)
    private List<MessageEntity> messages = new ArrayList<>();

    private LocalDateTime lastUpdated;
}
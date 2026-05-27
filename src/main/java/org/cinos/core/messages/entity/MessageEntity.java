package org.cinos.core.messages.entity;

import jakarta.persistence.*;
import lombok.*;
import org.cinos.core.messages.model.MessageStatus;
import org.cinos.core.users.entity.AccountEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "MESSAGES")
public class MessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ConversationEntity conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private AccountEntity sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private AccountEntity recipient;

    private String content;

    private LocalDateTime timestamp;

    private Boolean seen = false;

    @Enumerated(EnumType.STRING)
    private MessageStatus status;
}

package org.cinos.core.messages.repository;

import feign.Param;
import org.cinos.core.messages.entity.ConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<ConversationEntity, Long> {
    @Query("""
    SELECT c FROM ConversationEntity c
    JOIN c.participants p1
    JOIN c.participants p2
    WHERE p1.id = :userId1 AND p2.id = :userId2
    """)
    Optional<ConversationEntity> findByParticipants(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
    List<ConversationEntity> findByParticipants_Id(Long userId);
}

package org.cinos.core.follows.repository;

import org.cinos.core.follows.entity.FollowEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<FollowEntity, Long> {
    <T> Optional<T> findByFromUserIdAndToUserId(Long fromUserId, Long toUserId, Class<T> type);
    Optional<FollowEntity> findByFromUserIdAndToUserId(Long fromUserId, Long toUserId);
    List<FollowEntity> findByFromUserId(Long fromUserId);
    List<FollowEntity> findByToUserId(Long toUserId);

}

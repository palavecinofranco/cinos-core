package org.cinos.core.users.repository;

import org.cinos.core.users.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<AccountEntity, Long> {
    Optional<AccountEntity> findByUser_Id(Long id);
    List<AccountEntity> findByUser_UsernameContainingIgnoreCase(String username);
}

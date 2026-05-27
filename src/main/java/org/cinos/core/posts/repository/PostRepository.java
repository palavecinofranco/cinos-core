package org.cinos.core.posts.repository;

import org.cinos.core.posts.entity.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, Long>, JpaSpecificationExecutor<PostEntity> {

    List<PostEntity> findByUserAccount_Id(Long userId);
    List<PostEntity> findAllByUserAccount_IdAndActiveTrue(Long userId);
    Page<PostEntity> findAllByUserAccount_IdInOrderByPublicationDateDesc(List<Long> usersId, Pageable pageable);
    <T> List<T>  findByUsersSaved_Id(Long userId);

    // Métodos de búsqueda para posts
    @Query("SELECT p FROM PostEntity p WHERE p.active = true AND " +
           "(LOWER(p.make) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.model) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.year) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.fuel) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.transmission) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.motor) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.traccion) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<PostEntity> searchPosts(@Param("query") String query);

}

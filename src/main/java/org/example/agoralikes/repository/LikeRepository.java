package org.example.agoralikes.repository;

import org.example.agoralikes.entity.like.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LikeRepository extends JpaRepository<Like, Long> {

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    Long deleteByPostIdAndUserId(Long postId, Long userId);

    // В интерфейсе LikeRepository
    @Query("SELECT l.postId FROM Like l WHERE l.userId = :userId AND l.postId IN :postIds")
    List<Long> findPostIdsByUserIdAndPostIds(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);

}

package com.securops.modules.posts.repository;

import com.securops.modules.posts.entity.SecurityPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SecurityPostRepository extends JpaRepository<SecurityPost, Long> {
    Optional<SecurityPost> findByCode(String code);
    List<SecurityPost> findBySiteId(Long siteId);
    List<SecurityPost> findByActiveTrue();
}

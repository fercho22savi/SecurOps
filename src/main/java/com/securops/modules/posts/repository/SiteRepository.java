package com.securops.modules.posts.repository;

import com.securops.modules.posts.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {
    Optional<Site> findByNodeCode(String nodeCode);
}

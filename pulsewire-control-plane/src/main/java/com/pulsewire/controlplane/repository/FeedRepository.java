package com.pulsewire.controlplane.repository;

import com.pulsewire.controlplane.entity.Feed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {

    Optional<Feed> findByName(String name);

    List<Feed> findByVenue(String venue);

    List<Feed> findByFeedType(String feedType);

    List<Feed> findByEnabledTrue();

    boolean existsByName(String name);
}

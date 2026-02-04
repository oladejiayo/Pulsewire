package com.pulsewire.controlplane.service;

import com.pulsewire.controlplane.entity.Feed;
import com.pulsewire.controlplane.repository.FeedRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FeedService {

    private final FeedRepository repository;

    public FeedService(FeedRepository repository) {
        this.repository = repository;
    }

    public List<Feed> findAll() {
        return repository.findAll();
    }

    public List<Feed> findEnabled() {
        return repository.findByEnabledTrue();
    }

    public Optional<Feed> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<Feed> findByName(String name) {
        return repository.findByName(name);
    }

    public List<Feed> findByVenue(String venue) {
        return repository.findByVenue(venue);
    }

    public Feed create(Feed feed) {
        if (repository.existsByName(feed.getName())) {
            throw new IllegalArgumentException("Feed already exists: " + feed.getName());
        }
        return repository.save(feed);
    }

    public Feed update(Long id, Feed updated) {
        Feed existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feed not found: " + id));

        existing.setName(updated.getName());
        existing.setFeedType(updated.getFeedType());
        existing.setEndpointUrl(updated.getEndpointUrl());
        existing.setVenue(updated.getVenue());
        existing.setConfiguration(updated.getConfiguration());
        existing.setEnabled(updated.isEnabled());

        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public void disable(Long id) {
        Feed feed = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feed not found: " + id));
        feed.setEnabled(false);
        repository.save(feed);
    }

    public void enable(Long id) {
        Feed feed = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feed not found: " + id));
        feed.setEnabled(true);
        repository.save(feed);
    }
}

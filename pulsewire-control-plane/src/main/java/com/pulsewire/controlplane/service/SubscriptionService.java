package com.pulsewire.controlplane.service;

import com.pulsewire.controlplane.entity.Subscription;
import com.pulsewire.controlplane.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SubscriptionService {

    private final SubscriptionRepository repository;

    public SubscriptionService(SubscriptionRepository repository) {
        this.repository = repository;
    }

    public List<Subscription> findAll() {
        return repository.findAll();
    }

    public List<Subscription> findActive() {
        return repository.findByActiveTrue();
    }

    public Optional<Subscription> findById(Long id) {
        return repository.findById(id);
    }

    public List<Subscription> findByClientId(String clientId) {
        return repository.findByClientId(clientId);
    }

    public List<Subscription> findActiveByClientId(String clientId) {
        return repository.findByClientIdAndActiveTrue(clientId);
    }

    public Subscription create(Subscription subscription) {
        if (repository.existsByClientIdAndInstrumentId(
                subscription.getClientId(), subscription.getInstrumentId())) {
            throw new IllegalArgumentException("Subscription already exists for client " +
                    subscription.getClientId() + " and instrument " + subscription.getInstrumentId());
        }
        return repository.save(subscription);
    }

    public Subscription update(Long id, Subscription updated) {
        Subscription existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found: " + id));

        existing.setEventTypes(updated.getEventTypes());
        existing.setDepthLevel(updated.getDepthLevel());
        existing.setActive(updated.isActive());

        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public void deactivate(Long id) {
        Subscription subscription = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found: " + id));
        subscription.setActive(false);
        repository.save(subscription);
    }
}

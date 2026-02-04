package com.pulsewire.controlplane.repository;

import com.pulsewire.controlplane.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByClientId(String clientId);

    List<Subscription> findByInstrumentId(String instrumentId);

    List<Subscription> findByClientIdAndActiveTrue(String clientId);

    List<Subscription> findByActiveTrue();

    boolean existsByClientIdAndInstrumentId(String clientId, String instrumentId);
}

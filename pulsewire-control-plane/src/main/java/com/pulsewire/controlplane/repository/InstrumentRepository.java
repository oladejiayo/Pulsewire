package com.pulsewire.controlplane.repository;

import com.pulsewire.controlplane.entity.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstrumentRepository extends JpaRepository<Instrument, Long> {

    Optional<Instrument> findByInstrumentId(String instrumentId);

    List<Instrument> findByVenue(String venue);

    List<Instrument> findByAssetClass(String assetClass);

    List<Instrument> findByActiveTrue();

    boolean existsByInstrumentId(String instrumentId);
}

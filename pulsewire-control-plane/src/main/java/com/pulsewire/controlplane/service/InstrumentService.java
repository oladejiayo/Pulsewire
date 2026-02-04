package com.pulsewire.controlplane.service;

import com.pulsewire.controlplane.entity.Instrument;
import com.pulsewire.controlplane.repository.InstrumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InstrumentService {

    private final InstrumentRepository repository;

    public InstrumentService(InstrumentRepository repository) {
        this.repository = repository;
    }

    public List<Instrument> findAll() {
        return repository.findAll();
    }

    public List<Instrument> findActive() {
        return repository.findByActiveTrue();
    }

    public Optional<Instrument> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<Instrument> findByInstrumentId(String instrumentId) {
        return repository.findByInstrumentId(instrumentId);
    }

    public List<Instrument> findByVenue(String venue) {
        return repository.findByVenue(venue);
    }

    public Instrument create(Instrument instrument) {
        if (repository.existsByInstrumentId(instrument.getInstrumentId())) {
            throw new IllegalArgumentException("Instrument already exists: " + instrument.getInstrumentId());
        }
        return repository.save(instrument);
    }

    public Instrument update(Long id, Instrument updated) {
        Instrument existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Instrument not found: " + id));

        existing.setSymbol(updated.getSymbol());
        existing.setVenue(updated.getVenue());
        existing.setAssetClass(updated.getAssetClass());
        existing.setCurrency(updated.getCurrency());
        existing.setTickSize(updated.getTickSize());
        existing.setIsin(updated.getIsin());
        existing.setFigi(updated.getFigi());
        existing.setActive(updated.isActive());

        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public void deactivate(Long id) {
        Instrument instrument = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Instrument not found: " + id));
        instrument.setActive(false);
        repository.save(instrument);
    }
}

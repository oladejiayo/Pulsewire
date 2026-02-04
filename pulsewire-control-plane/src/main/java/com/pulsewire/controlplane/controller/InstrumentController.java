package com.pulsewire.controlplane.controller;

import com.pulsewire.controlplane.entity.Instrument;
import com.pulsewire.controlplane.service.InstrumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instruments")
public class InstrumentController {

    private final InstrumentService service;

    public InstrumentController(InstrumentService service) {
        this.service = service;
    }

    @GetMapping
    public List<Instrument> findAll(@RequestParam(required = false) String venue,
                                     @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        if (venue != null) {
            return service.findByVenue(venue);
        }
        return activeOnly ? service.findActive() : service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Instrument> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-instrument-id/{instrumentId}")
    public ResponseEntity<Instrument> findByInstrumentId(@PathVariable String instrumentId) {
        return service.findByInstrumentId(instrumentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Instrument> create(@RequestBody Instrument instrument) {
        Instrument created = service.create(instrument);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Instrument> update(@PathVariable Long id, @RequestBody Instrument instrument) {
        Instrument updated = service.update(id, instrument);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        service.deactivate(id);
        return ResponseEntity.ok().build();
    }
}

package com.pulsewire.controlplane.controller;

import com.pulsewire.controlplane.entity.Feed;
import com.pulsewire.controlplane.service.FeedService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feeds")
public class FeedController {

    private final FeedService service;

    public FeedController(FeedService service) {
        this.service = service;
    }

    @GetMapping
    public List<Feed> findAll(@RequestParam(required = false) String venue,
                               @RequestParam(required = false, defaultValue = "false") boolean enabledOnly) {
        if (venue != null) {
            return service.findByVenue(venue);
        }
        return enabledOnly ? service.findEnabled() : service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Feed> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-name/{name}")
    public ResponseEntity<Feed> findByName(@PathVariable String name) {
        return service.findByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Feed> create(@RequestBody Feed feed) {
        Feed created = service.create(feed);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Feed> update(@PathVariable Long id, @RequestBody Feed feed) {
        Feed updated = service.update(id, feed);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/enable")
    public ResponseEntity<Void> enable(@PathVariable Long id) {
        service.enable(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/disable")
    public ResponseEntity<Void> disable(@PathVariable Long id) {
        service.disable(id);
        return ResponseEntity.ok().build();
    }
}

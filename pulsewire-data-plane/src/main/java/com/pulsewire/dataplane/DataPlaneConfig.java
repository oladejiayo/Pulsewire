package com.pulsewire.dataplane;

import com.pulsewire.core.backbone.BackbonePublisher;
import com.pulsewire.dataplane.adapter.FeedAdapter;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Data plane configuration that wires feed adapters to the backbone.
 * BackbonePublisher/Consumer beans are provided by core module's config classes
 * (InMemoryBackboneConfig or KafkaBackboneConfig) based on profile.
 */
@Configuration
public class DataPlaneConfig {

    private static final Logger log = LoggerFactory.getLogger(DataPlaneConfig.class);

    private final List<FeedAdapter> adapters;
    private final BackbonePublisher publisher;

    public DataPlaneConfig(List<FeedAdapter> adapters, BackbonePublisher publisher) {
        this.adapters = adapters;
        this.publisher = publisher;
    }

    @PostConstruct
    public void startAdapters() {
        for (FeedAdapter adapter : adapters) {
            adapter.start(publisher);
            log.info("Started adapter: {}", adapter.getClass().getSimpleName());
        }
    }

    @PreDestroy
    public void stopAdapters() {
        for (FeedAdapter adapter : adapters) {
            adapter.stop();
        }
    }
}

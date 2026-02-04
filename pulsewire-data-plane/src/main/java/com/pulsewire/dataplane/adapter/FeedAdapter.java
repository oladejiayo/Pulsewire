package com.pulsewire.dataplane.adapter;

import com.pulsewire.core.backbone.BackbonePublisher;
import com.pulsewire.core.model.MarketEvent;

/**
 * SPI for feed adapters.
 */
public interface FeedAdapter {

    /**
     * Start ingesting data and publishing to the backbone.
     *
     * @param publisher backbone to publish raw events
     */
    void start(BackbonePublisher publisher);

    /**
     * Stop ingestion.
     */
    void stop();

    /**
     * @return true if the adapter is currently running
     */
    boolean isRunning();
}

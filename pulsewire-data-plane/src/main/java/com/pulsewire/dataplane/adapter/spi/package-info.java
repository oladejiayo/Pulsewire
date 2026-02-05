/**
 * Feed Adapter Service Provider Interface (SPI) for PulseWire.
 * 
 * <p>This package defines the contract for integrating market data feeds
 * into the PulseWire ingestion pipeline. New feed sources (exchanges, vendors)
 * can be onboarded by implementing the {@link FeedAdapter} interface.
 * 
 * <h2>Core Interfaces</h2>
 * <ul>
 *   <li>{@link FeedAdapter} - Lifecycle management: connect, disconnect, heartbeat</li>
 *   <li>{@link FeedEventHandler} - Callback interface for events and messages</li>
 *   <li>{@link RawFeedMessage} - Transport-agnostic message container</li>
 *   <li>{@link TransportType} - Metadata enum for configuration and metrics</li>
 * </ul>
 * 
 * <h2>Implementation Guide</h2>
 * <ol>
 *   <li>Implement {@link FeedAdapter} for your feed source</li>
 *   <li>Register via {@code META-INF/services/com.pulsewire.dataplane.adapter.spi.FeedAdapter}</li>
 *   <li>Configure in the control plane's feed management</li>
 * </ol>
 * 
 * <h2>Example</h2>
 * <pre>{@code
 * public class MyExchangeAdapter implements FeedAdapter {
 *     private final String id = "my-exchange-" + UUID.randomUUID();
 *     
 *     @Override
 *     public String getId() { return id; }
 *     
 *     @Override
 *     public TransportType getTransportType() { return TransportType.TCP; }
 *     
 *     @Override
 *     public void connect(FeedEventHandler handler) {
 *         // Establish connection...
 *         handler.onConnected(id);
 *     }
 *     
 *     // ... implement remaining methods
 * }
 * }</pre>
 * 
 * @see FeedAdapter
 * @see SyntheticFeedAdapterV2 for a complete example implementation
 */
package com.pulsewire.dataplane.adapter.spi;

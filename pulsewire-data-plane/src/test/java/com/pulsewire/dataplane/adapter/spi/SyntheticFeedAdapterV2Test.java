package com.pulsewire.dataplane.adapter.spi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the example SyntheticFeedAdapter implementation.
 * 
 * AC3: Example implementation compiles and can be wired into the ingestion pipeline.
 * These tests verify the adapter correctly implements the SPI contract.
 */
@DisplayName("SyntheticFeedAdapter Example Implementation Tests")
class SyntheticFeedAdapterV2Test {

    private SyntheticFeedAdapterV2 adapter;
    private TestFeedEventHandler handler;

    @BeforeEach
    void setUp() {
        adapter = new SyntheticFeedAdapterV2();
        handler = new TestFeedEventHandler();
    }

    /**
     * AC3: Verify example implementation implements the SPI interface.
     */
    @Nested
    @DisplayName("SPI Compliance")
    class SpiCompliance {

        @Test
        @DisplayName("SyntheticFeedAdapterV2 should implement FeedAdapter interface")
        void shouldImplementFeedAdapterInterface() {
            assertTrue(adapter instanceof FeedAdapter,
                "Example adapter must implement the FeedAdapter SPI");
        }

        @Test
        @DisplayName("Adapter should have a unique ID")
        void shouldHaveUniqueId() {
            String id = adapter.getId();
            assertNotNull(id, "Adapter ID should not be null");
            assertFalse(id.isBlank(), "Adapter ID should not be blank");
            
            // Create another adapter and verify IDs are different
            SyntheticFeedAdapterV2 adapter2 = new SyntheticFeedAdapterV2();
            assertNotEquals(adapter.getId(), adapter2.getId(),
                "Each adapter instance should have a unique ID");
        }

        @Test
        @DisplayName("Adapter should declare its transport type")
        void shouldDeclareTransportType() {
            TransportType type = adapter.getTransportType();
            assertNotNull(type, "Transport type should not be null");
            // Synthetic adapter simulates internal events, so VENDOR_SDK is appropriate
            // but any valid type is acceptable for this test
        }
    }

    /**
     * AC3: Verify lifecycle operations work correctly.
     */
    @Nested
    @DisplayName("Lifecycle Operations")
    class LifecycleOperations {

        @Test
        @DisplayName("Adapter should start disconnected")
        void shouldStartDisconnected() {
            assertFalse(adapter.isConnected(),
                "Adapter should not be connected before connect() is called");
        }

        @Test
        @DisplayName("Connect should invoke onConnected callback")
        @Timeout(value = 2, unit = TimeUnit.SECONDS)
        void connectShouldInvokeCallback() throws InterruptedException {
            adapter.connect(handler);
            
            // Wait for async connection
            assertTrue(handler.awaitConnected(1, TimeUnit.SECONDS),
                "onConnected should be called after connect()");
            assertEquals(adapter.getId(), handler.connectedAdapterId.get(),
                "Callback should receive correct adapter ID");
        }

        @Test
        @DisplayName("Adapter should be connected after connect")
        @Timeout(value = 2, unit = TimeUnit.SECONDS)
        void shouldBeConnectedAfterConnect() throws InterruptedException {
            adapter.connect(handler);
            handler.awaitConnected(1, TimeUnit.SECONDS);
            
            assertTrue(adapter.isConnected(),
                "isConnected should return true after successful connect");
        }

        @Test
        @DisplayName("Disconnect should invoke onDisconnected callback")
        @Timeout(value = 2, unit = TimeUnit.SECONDS)
        void disconnectShouldInvokeCallback() throws InterruptedException {
            adapter.connect(handler);
            handler.awaitConnected(1, TimeUnit.SECONDS);
            
            adapter.disconnect();
            
            assertTrue(handler.awaitDisconnected(1, TimeUnit.SECONDS),
                "onDisconnected should be called after disconnect()");
        }

        @Test
        @DisplayName("Adapter should not be connected after disconnect")
        @Timeout(value = 2, unit = TimeUnit.SECONDS)
        void shouldNotBeConnectedAfterDisconnect() throws InterruptedException {
            adapter.connect(handler);
            handler.awaitConnected(1, TimeUnit.SECONDS);
            adapter.disconnect();
            handler.awaitDisconnected(1, TimeUnit.SECONDS);
            
            assertFalse(adapter.isConnected(),
                "isConnected should return false after disconnect");
        }

        @Test
        @DisplayName("Connect with null handler should throw")
        void connectWithNullHandlerShouldThrow() {
            assertThrows(IllegalArgumentException.class,
                () -> adapter.connect(null),
                "connect() should reject null handler");
        }

        @Test
        @DisplayName("Connect when already connected should throw")
        @Timeout(value = 2, unit = TimeUnit.SECONDS)
        void connectWhenConnectedShouldThrow() throws InterruptedException {
            adapter.connect(handler);
            handler.awaitConnected(1, TimeUnit.SECONDS);
            
            assertThrows(IllegalStateException.class,
                () -> adapter.connect(handler),
                "connect() should throw if already connected");
        }
    }

    /**
     * AC3: Verify message generation for pipeline wiring.
     */
    @Nested
    @DisplayName("Message Generation")
    class MessageGeneration {

        @Test
        @DisplayName("Connected adapter should emit messages")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        void shouldEmitMessages() throws InterruptedException {
            adapter.connect(handler);
            handler.awaitConnected(1, TimeUnit.SECONDS);
            
            // Wait for at least one message
            assertTrue(handler.awaitMessage(3, TimeUnit.SECONDS),
                "Adapter should emit messages when connected");
            
            RawFeedMessage message = handler.lastMessage.get();
            assertNotNull(message, "Message should not be null");
            assertNotNull(message.payload(), "Message payload should not be null");
            assertTrue(message.payload().length > 0, "Message payload should not be empty");
            assertNotNull(message.receiveTimestamp(), "Message should have timestamp");
        }

        @Test
        @DisplayName("Messages should have monotonically increasing sequence numbers")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        void messagesShouldHaveSequenceNumbers() throws InterruptedException {
            adapter.connect(handler);
            handler.awaitConnected(1, TimeUnit.SECONDS);
            
            // Wait for first message
            handler.awaitMessage(3, TimeUnit.SECONDS);
            long firstSeq = handler.lastMessage.get().sequenceNumber();
            
            // Wait for second message
            handler.messageCount.set(0);
            handler.messageLatch = new CountDownLatch(1);
            handler.awaitMessage(3, TimeUnit.SECONDS);
            long secondSeq = handler.lastMessage.get().sequenceNumber();
            
            assertTrue(secondSeq > firstSeq,
                "Sequence numbers should be monotonically increasing");
        }

        @Test
        @DisplayName("Disconnected adapter should not emit messages")
        @Timeout(value = 3, unit = TimeUnit.SECONDS)
        void disconnectedAdapterShouldNotEmitMessages() throws InterruptedException {
            adapter.connect(handler);
            handler.awaitConnected(1, TimeUnit.SECONDS);
            adapter.disconnect();
            handler.awaitDisconnected(1, TimeUnit.SECONDS);
            
            // Reset and wait - should not receive messages
            handler.messageCount.set(0);
            handler.messageLatch = new CountDownLatch(1);
            
            assertFalse(handler.awaitMessage(500, TimeUnit.MILLISECONDS),
                "Adapter should not emit messages after disconnect");
        }
    }

    /**
     * AC3: Verify heartbeat functionality.
     */
    @Nested
    @DisplayName("Heartbeat Support")
    class HeartbeatSupport {

        @Test
        @DisplayName("sendHeartbeat should not throw when connected")
        @Timeout(value = 2, unit = TimeUnit.SECONDS)
        void sendHeartbeatShouldNotThrowWhenConnected() throws InterruptedException {
            adapter.connect(handler);
            handler.awaitConnected(1, TimeUnit.SECONDS);
            
            assertDoesNotThrow(() -> adapter.sendHeartbeat(),
                "sendHeartbeat should succeed when connected");
        }

        @Test
        @DisplayName("sendHeartbeat should throw when disconnected")
        void sendHeartbeatShouldThrowWhenDisconnected() {
            assertThrows(IllegalStateException.class,
                () -> adapter.sendHeartbeat(),
                "sendHeartbeat should throw when not connected");
        }
    }

    /**
     * Test helper: A handler that captures callbacks for verification.
     */
    static class TestFeedEventHandler implements FeedEventHandler {
        final AtomicReference<String> connectedAdapterId = new AtomicReference<>();
        final AtomicReference<String> disconnectedAdapterId = new AtomicReference<>();
        final AtomicReference<String> disconnectReason = new AtomicReference<>();
        final AtomicReference<RawFeedMessage> lastMessage = new AtomicReference<>();
        final AtomicReference<Throwable> lastError = new AtomicReference<>();
        final AtomicBoolean heartbeatTimeoutReceived = new AtomicBoolean(false);
        final java.util.concurrent.atomic.AtomicInteger messageCount = 
            new java.util.concurrent.atomic.AtomicInteger(0);
        
        CountDownLatch connectLatch = new CountDownLatch(1);
        CountDownLatch disconnectLatch = new CountDownLatch(1);
        CountDownLatch messageLatch = new CountDownLatch(1);
        
        @Override
        public void onConnected(String adapterId) {
            connectedAdapterId.set(adapterId);
            connectLatch.countDown();
        }
        
        @Override
        public void onDisconnected(String adapterId, String reason) {
            disconnectedAdapterId.set(adapterId);
            disconnectReason.set(reason);
            disconnectLatch.countDown();
        }
        
        @Override
        public void onMessage(String adapterId, RawFeedMessage message) {
            lastMessage.set(message);
            messageCount.incrementAndGet();
            messageLatch.countDown();
        }
        
        @Override
        public void onError(String adapterId, Throwable error) {
            lastError.set(error);
        }
        
        @Override
        public void onHeartbeatTimeout(String adapterId) {
            heartbeatTimeoutReceived.set(true);
        }
        
        boolean awaitConnected(long timeout, TimeUnit unit) throws InterruptedException {
            return connectLatch.await(timeout, unit);
        }
        
        boolean awaitDisconnected(long timeout, TimeUnit unit) throws InterruptedException {
            return disconnectLatch.await(timeout, unit);
        }
        
        boolean awaitMessage(long timeout, TimeUnit unit) throws InterruptedException {
            return messageLatch.await(timeout, unit);
        }
    }
}

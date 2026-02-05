package com.pulsewire.dataplane.adapter.spi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the FeedAdapter SPI contract.
 * 
 * These tests verify that the SPI interface exists and has the required methods
 * per acceptance criteria:
 * - AC1: SPI covers connect, disconnect, heartbeat, message receive, and error callbacks
 * - AC2: SPI supports multiple transport types without leaking transport details
 */
@DisplayName("FeedAdapter SPI Contract Tests")
class FeedAdapterSpiTest {

    /**
     * AC1: Verify the FeedAdapter interface declares all required lifecycle methods.
     * The SPI must support: connect, disconnect, heartbeat operations.
     */
    @Nested
    @DisplayName("AC1: Lifecycle Methods")
    class LifecycleMethods {

        @Test
        @DisplayName("FeedAdapter interface should exist")
        void feedAdapterInterfaceShouldExist() {
            // Verify the interface is accessible - compilation proves existence
            assertNotNull(FeedAdapter.class);
            assertTrue(FeedAdapter.class.isInterface(), 
                "FeedAdapter must be an interface for SPI extensibility");
        }

        @Test
        @DisplayName("FeedAdapter should have getId method for unique identification")
        void shouldHaveGetIdMethod() throws NoSuchMethodException {
            // Each adapter instance needs a unique ID for logging, metrics, and management
            var method = FeedAdapter.class.getMethod("getId");
            assertEquals(String.class, method.getReturnType(),
                "getId should return String for adapter identification");
        }

        @Test
        @DisplayName("FeedAdapter should have connect method accepting event handler")
        void shouldHaveConnectMethod() throws NoSuchMethodException {
            // connect() initiates connection and registers callback handler
            var method = FeedAdapter.class.getMethod("connect", FeedEventHandler.class);
            assertEquals(void.class, method.getReturnType(),
                "connect is an action method, should return void");
        }

        @Test
        @DisplayName("FeedAdapter should have disconnect method")
        void shouldHaveDisconnectMethod() throws NoSuchMethodException {
            // disconnect() gracefully closes connection
            var method = FeedAdapter.class.getMethod("disconnect");
            assertEquals(void.class, method.getReturnType(),
                "disconnect is an action method, should return void");
        }

        @Test
        @DisplayName("FeedAdapter should have isConnected method for state query")
        void shouldHaveIsConnectedMethod() throws NoSuchMethodException {
            // isConnected() allows checking current connection state
            var method = FeedAdapter.class.getMethod("isConnected");
            assertEquals(boolean.class, method.getReturnType(),
                "isConnected should return primitive boolean for performance");
        }

        @Test
        @DisplayName("FeedAdapter should have sendHeartbeat method")
        void shouldHaveSendHeartbeatMethod() throws NoSuchMethodException {
            // sendHeartbeat() maintains connection liveness
            var method = FeedAdapter.class.getMethod("sendHeartbeat");
            assertEquals(void.class, method.getReturnType(),
                "sendHeartbeat is an action method, should return void");
        }
    }

    /**
     * AC2: Verify transport type support without leaking details.
     */
    @Nested
    @DisplayName("AC2: Transport Abstraction")
    class TransportAbstraction {

        @Test
        @DisplayName("TransportType enum should exist with required transport types")
        void transportTypeEnumShouldExist() {
            // Verify enum exists and contains all required transport types
            assertNotNull(TransportType.class);
            assertTrue(TransportType.class.isEnum(),
                "TransportType must be an enum for type safety");
        }

        @Test
        @DisplayName("TransportType should include TCP")
        void shouldIncludeTcp() {
            assertNotNull(TransportType.valueOf("TCP"),
                "TCP is a common transport for market data feeds");
        }

        @Test
        @DisplayName("TransportType should include UDP")
        void shouldIncludeUdp() {
            assertNotNull(TransportType.valueOf("UDP"),
                "UDP multicast is used for high-throughput market data");
        }

        @Test
        @DisplayName("TransportType should include WEBSOCKET")
        void shouldIncludeWebSocket() {
            assertNotNull(TransportType.valueOf("WEBSOCKET"),
                "WebSocket is common for browser and modern API integration");
        }

        @Test
        @DisplayName("TransportType should include VENDOR_SDK")
        void shouldIncludeVendorSdk() {
            assertNotNull(TransportType.valueOf("VENDOR_SDK"),
                "Vendor SDKs abstract proprietary protocols (Bloomberg, Reuters)");
        }

        @Test
        @DisplayName("FeedAdapter should expose transport type for metrics/config")
        void feedAdapterShouldExposeTransportType() throws NoSuchMethodException {
            // Transport type is exposed for configuration and metrics tagging
            // but NOT for transport-specific behavior in downstream stages
            var method = FeedAdapter.class.getMethod("getTransportType");
            assertEquals(TransportType.class, method.getReturnType(),
                "getTransportType should return TransportType enum");
        }
    }

    /**
     * AC1: Verify callback handler interface for message receive and error callbacks.
     */
    @Nested
    @DisplayName("AC1: Event Handler Callbacks")
    class EventHandlerCallbacks {

        @Test
        @DisplayName("FeedEventHandler interface should exist")
        void feedEventHandlerShouldExist() {
            assertNotNull(FeedEventHandler.class);
            assertTrue(FeedEventHandler.class.isInterface(),
                "FeedEventHandler must be an interface for flexibility");
        }

        @Test
        @DisplayName("FeedEventHandler should have onConnected callback")
        void shouldHaveOnConnectedCallback() throws NoSuchMethodException {
            // onConnected is invoked when adapter successfully connects
            var method = FeedEventHandler.class.getMethod("onConnected", String.class);
            assertEquals(void.class, method.getReturnType());
        }

        @Test
        @DisplayName("FeedEventHandler should have onDisconnected callback")
        void shouldHaveOnDisconnectedCallback() throws NoSuchMethodException {
            // onDisconnected is invoked when connection drops (intentional or not)
            var method = FeedEventHandler.class.getMethod("onDisconnected", String.class, String.class);
            assertEquals(void.class, method.getReturnType());
        }

        @Test
        @DisplayName("FeedEventHandler should have onMessage callback for raw messages")
        void shouldHaveOnMessageCallback() throws NoSuchMethodException {
            // onMessage receives raw feed data - transport-agnostic
            var method = FeedEventHandler.class.getMethod("onMessage", String.class, RawFeedMessage.class);
            assertEquals(void.class, method.getReturnType());
        }

        @Test
        @DisplayName("FeedEventHandler should have onError callback")
        void shouldHaveOnErrorCallback() throws NoSuchMethodException {
            // onError handles exceptions during connection or message processing
            var method = FeedEventHandler.class.getMethod("onError", String.class, Throwable.class);
            assertEquals(void.class, method.getReturnType());
        }

        @Test
        @DisplayName("FeedEventHandler should have onHeartbeatTimeout callback")
        void shouldHaveOnHeartbeatTimeoutCallback() throws NoSuchMethodException {
            // onHeartbeatTimeout detects stale connections
            var method = FeedEventHandler.class.getMethod("onHeartbeatTimeout", String.class);
            assertEquals(void.class, method.getReturnType());
        }
    }

    /**
     * AC2: Verify RawFeedMessage doesn't leak transport details.
     */
    @Nested
    @DisplayName("AC2: RawFeedMessage Transport Agnostic")
    class RawFeedMessageTests {

        @Test
        @DisplayName("RawFeedMessage should exist for transport-agnostic message passing")
        void rawFeedMessageShouldExist() {
            assertNotNull(RawFeedMessage.class);
        }

        @Test
        @DisplayName("RawFeedMessage should carry raw payload bytes")
        void shouldHavePayload() throws NoSuchMethodException {
            // Raw bytes allow downstream to handle any message format
            var method = RawFeedMessage.class.getMethod("payload");
            assertEquals(byte[].class, method.getReturnType(),
                "Payload should be raw bytes for format flexibility");
        }

        @Test
        @DisplayName("RawFeedMessage should carry receive timestamp")
        void shouldHaveReceiveTimestamp() throws NoSuchMethodException {
            // Receive timestamp is critical for latency measurement
            var method = RawFeedMessage.class.getMethod("receiveTimestamp");
            assertEquals(java.time.Instant.class, method.getReturnType(),
                "Timestamp should use Instant for nanosecond precision");
        }

        @Test
        @DisplayName("RawFeedMessage should carry sequence number for gap detection")
        void shouldHaveSequenceNumber() throws NoSuchMethodException {
            // Sequence number enables gap detection and ordering
            var method = RawFeedMessage.class.getMethod("sequenceNumber");
            assertEquals(long.class, method.getReturnType(),
                "Sequence number should be primitive long for performance");
        }
    }
}

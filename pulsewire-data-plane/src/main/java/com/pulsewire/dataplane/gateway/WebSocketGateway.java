package com.pulsewire.dataplane.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsewire.core.backbone.BackboneConsumer;
import com.pulsewire.core.model.MarketEvent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket gateway that streams market events to connected clients.
 */
@Component
public class WebSocketGateway extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(WebSocketGateway.class);
    private static final String CANONICAL_TOPIC = "canonical.events";

    private final BackboneConsumer consumer;
    private final ObjectMapper objectMapper;
    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private final Map<WebSocketSession, Set<String>> subscriptions = new ConcurrentHashMap<>();

    public WebSocketGateway(BackboneConsumer consumer, ObjectMapper objectMapper) {
        this.consumer = consumer;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void start() {
        consumer.subscribe(CANONICAL_TOPIC, this::broadcastEvent);
        log.info("WebSocketGateway started, listening to canonical events");
    }

    @PreDestroy
    public void stop() {
        consumer.unsubscribe(CANONICAL_TOPIC);
        log.info("WebSocketGateway stopped");
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        subscriptions.put(session, ConcurrentHashMap.newKeySet());
        log.info("WebSocket connection established: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        subscriptions.remove(session);
        log.info("WebSocket connection closed: {} with status {}", session.getId(), status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            SubscriptionRequest request = objectMapper.readValue(payload, SubscriptionRequest.class);

            Set<String> sessionSubs = subscriptions.get(session);
            if (sessionSubs == null) return;

            if ("subscribe".equalsIgnoreCase(request.action())) {
                sessionSubs.add(request.instrumentId());
                log.info("Session {} subscribed to {}", session.getId(), request.instrumentId());
                sendMessage(session, new SubscriptionResponse("subscribed", request.instrumentId()));
            } else if ("unsubscribe".equalsIgnoreCase(request.action())) {
                sessionSubs.remove(request.instrumentId());
                log.info("Session {} unsubscribed from {}", session.getId(), request.instrumentId());
                sendMessage(session, new SubscriptionResponse("unsubscribed", request.instrumentId()));
            }
        } catch (Exception e) {
            log.error("Error handling message from session {}", session.getId(), e);
        }
    }

    private void broadcastEvent(MarketEvent event) {
        String instrumentId = event.instrumentId();

        for (WebSocketSession session : sessions) {
            Set<String> subs = subscriptions.get(session);
            // Send if subscribed to this instrument or has wildcard subscription "*"
            if (subs != null && (subs.contains(instrumentId) || subs.contains("*"))) {
                sendEvent(session, event);
            }
        }
    }

    private void sendEvent(WebSocketSession session, MarketEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            log.error("Error sending event to session {}", session.getId(), e);
        }
    }

    private void sendMessage(WebSocketSession session, Object message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            log.error("Error sending message to session {}", session.getId(), e);
        }
    }

    public int getActiveConnections() {
        return sessions.size();
    }

    // DTO records
    public record SubscriptionRequest(String action, String instrumentId) {}
    public record SubscriptionResponse(String status, String instrumentId) {}
}

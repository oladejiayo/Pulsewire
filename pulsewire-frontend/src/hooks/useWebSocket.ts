import { useEffect, useRef, useState, useCallback } from 'react';
import type { MarketEvent, SubscriptionRequest, SubscriptionResponse } from '../types';

type ConnectionStatus = 'connecting' | 'connected' | 'disconnected' | 'error';

interface UseWebSocketReturn {
  status: ConnectionStatus;
  events: Map<string, MarketEvent>;
  subscribe: (symbols: string[]) => void;
  unsubscribe: (symbols: string[]) => void;
}

export function useMarketDataWebSocket(): UseWebSocketReturn {
  const [status, setStatus] = useState<ConnectionStatus>('disconnected');
  const [events, setEvents] = useState<Map<string, MarketEvent>>(new Map());
  const wsRef = useRef<WebSocket | null>(null);
  const reconnectTimeoutRef = useRef<number | null>(null);

  const connect = useCallback(() => {
    if (wsRef.current?.readyState === WebSocket.OPEN) return;

    setStatus('connecting');
    
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.host}/ws/market-data`;
    
    const ws = new WebSocket(wsUrl);
    wsRef.current = ws;

    ws.onopen = () => {
      setStatus('connected');
      console.log('WebSocket connected');
    };

    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        
        // Check if it's a subscription response
        if (data.status && data.symbols) {
          const response = data as SubscriptionResponse;
          console.log('Subscription response:', response);
          return;
        }

        // It's a market event
        const marketEvent = data as MarketEvent;
        setEvents((prev) => {
          const updated = new Map(prev);
          updated.set(marketEvent.symbol, marketEvent);
          return updated;
        });
      } catch (err) {
        console.error('Failed to parse WebSocket message:', err);
      }
    };

    ws.onclose = () => {
      setStatus('disconnected');
      console.log('WebSocket disconnected');
      
      // Attempt reconnection after 3 seconds
      reconnectTimeoutRef.current = window.setTimeout(() => {
        connect();
      }, 3000);
    };

    ws.onerror = (error) => {
      setStatus('error');
      console.error('WebSocket error:', error);
    };
  }, []);

  const disconnect = useCallback(() => {
    if (reconnectTimeoutRef.current) {
      clearTimeout(reconnectTimeoutRef.current);
    }
    if (wsRef.current) {
      wsRef.current.close();
      wsRef.current = null;
    }
  }, []);

  const sendMessage = useCallback((message: SubscriptionRequest) => {
    if (wsRef.current?.readyState === WebSocket.OPEN) {
      wsRef.current.send(JSON.stringify(message));
    }
  }, []);

  const subscribe = useCallback((symbols: string[]) => {
    sendMessage({ action: 'subscribe', symbols });
  }, [sendMessage]);

  const unsubscribe = useCallback((symbols: string[]) => {
    sendMessage({ action: 'unsubscribe', symbols });
  }, [sendMessage]);

  useEffect(() => {
    connect();
    return () => disconnect();
  }, [connect, disconnect]);

  return { status, events, subscribe, unsubscribe };
}

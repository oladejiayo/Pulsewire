// API Types matching backend entities

export interface Instrument {
  id: number;
  symbol: string;
  name: string;
  type: string;
  exchange: string;
  active: boolean;
}

export interface Feed {
  id: number;
  name: string;
  provider: string;
  protocol: string;
  endpoint: string;
  enabled: boolean;
}

export interface Subscription {
  id: number;
  instrumentId: number;
  feedId: number;
  priority: number;
  active: boolean;
}

// Market Data Types
export interface MarketEvent {
  eventType: 'TRADE' | 'QUOTE';
  symbol: string;
  timestamp: string;
  source: string;
  payload: Trade | Quote;
}

export interface Trade {
  price: number;
  size: number;
  side: 'BUY' | 'SELL';
}

export interface Quote {
  bidPrice: number;
  bidSize: number;
  askPrice: number;
  askSize: number;
}

// WebSocket Messages
export interface SubscriptionRequest {
  action: 'subscribe' | 'unsubscribe';
  symbols: string[];
}

export interface SubscriptionResponse {
  status: string;
  symbols: string[];
}

import { useState, useEffect } from 'react';
import { Activity, TrendingUp, TrendingDown, Wifi, WifiOff } from 'lucide-react';
import { useMarketDataWebSocket } from '../hooks/useWebSocket';
import type { MarketEvent, Trade, Quote } from '../types';

// Default symbols for demo
const DEFAULT_SYMBOLS = ['AAPL', 'GOOGL', 'MSFT', 'AMZN', 'META', 'NVDA', 'TSLA', 'JPM'];

export default function Dashboard() {
  const { status, events, subscribe } = useMarketDataWebSocket();
  const [prevPrices, setPrevPrices] = useState<Map<string, number>>(new Map());
  const [priceChanges, setPriceChanges] = useState<Map<string, 'up' | 'down' | null>>(new Map());

  // Subscribe to default symbols on connect
  useEffect(() => {
    if (status === 'connected') {
      subscribe(DEFAULT_SYMBOLS);
    }
  }, [status, subscribe]);

  // Track price changes for animations
  useEffect(() => {
    events.forEach((event, symbol) => {
      const currentPrice = getPrice(event);
      const prevPrice = prevPrices.get(symbol);

      if (prevPrice !== undefined && currentPrice !== prevPrice) {
        setPriceChanges((prev) => {
          const updated = new Map(prev);
          updated.set(symbol, currentPrice > prevPrice ? 'up' : 'down');
          return updated;
        });

        // Clear animation after 500ms
        setTimeout(() => {
          setPriceChanges((prev) => {
            const updated = new Map(prev);
            updated.set(symbol, null);
            return updated;
          });
        }, 500);
      }

      setPrevPrices((prev) => {
        const updated = new Map(prev);
        updated.set(symbol, currentPrice);
        return updated;
      });
    });
  }, [events, prevPrices]);

  function getPrice(event: MarketEvent): number {
    if (event.eventType === 'TRADE') {
      return (event.payload as Trade).price;
    }
    const quote = event.payload as Quote;
    return (quote.bidPrice + quote.askPrice) / 2;
  }

  function formatPrice(price: number): string {
    return price.toLocaleString('en-US', {
      style: 'currency',
      currency: 'USD',
    });
  }

  function formatTime(timestamp: string): string {
    return new Date(timestamp).toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    });
  }

  const connectionColor = {
    connected: 'text-green-500',
    connecting: 'text-yellow-500',
    disconnected: 'text-red-500',
    error: 'text-red-500',
  };

  return (
    <div className="p-8">
      {/* Header */}
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold text-slate-800">Live Market Data</h1>
          <p className="text-slate-500 mt-1">Real-time price updates via WebSocket</p>
        </div>
        <div className={`flex items-center gap-2 ${connectionColor[status]}`}>
          {status === 'connected' ? (
            <Wifi className="w-5 h-5" />
          ) : (
            <WifiOff className="w-5 h-5" />
          )}
          <span className="text-sm font-medium capitalize">{status}</span>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <StatCard
          title="Active Symbols"
          value={events.size.toString()}
          icon={<Activity className="w-6 h-6" />}
          color="bg-blue-500"
        />
        <StatCard
          title="Total Events"
          value="—"
          icon={<TrendingUp className="w-6 h-6" />}
          color="bg-green-500"
        />
        <StatCard
          title="Trades"
          value={Array.from(events.values()).filter((e) => e.eventType === 'TRADE').length.toString()}
          icon={<TrendingUp className="w-6 h-6" />}
          color="bg-purple-500"
        />
        <StatCard
          title="Quotes"
          value={Array.from(events.values()).filter((e) => e.eventType === 'QUOTE').length.toString()}
          icon={<TrendingDown className="w-6 h-6" />}
          color="bg-orange-500"
        />
      </div>

      {/* Market Data Grid */}
      <div className="bg-white rounded-xl shadow-sm border border-slate-200">
        <div className="p-4 border-b border-slate-200">
          <h2 className="text-lg font-semibold text-slate-800">Live Prices</h2>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-slate-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  Symbol
                </th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  Type
                </th>
                <th className="px-6 py-3 text-right text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  Price
                </th>
                <th className="px-6 py-3 text-right text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  Bid / Ask
                </th>
                <th className="px-6 py-3 text-right text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  Size
                </th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  Source
                </th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  Time
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {events.size === 0 ? (
                <tr>
                  <td colSpan={7} className="px-6 py-12 text-center text-slate-400">
                    {status === 'connected' 
                      ? 'Waiting for market data...'
                      : 'Connecting to market data feed...'}
                  </td>
                </tr>
              ) : (
                Array.from(events.entries()).map(([symbol, event]) => {
                  const priceChange = priceChanges.get(symbol);
                  const isTrade = event.eventType === 'TRADE';
                  const trade = isTrade ? (event.payload as Trade) : null;
                  const quote = !isTrade ? (event.payload as Quote) : null;

                  return (
                    <tr
                      key={symbol}
                      className={`hover:bg-slate-50 ${
                        priceChange === 'up' ? 'price-up' : priceChange === 'down' ? 'price-down' : ''
                      }`}
                    >
                      <td className="px-6 py-4">
                        <span className="font-semibold text-slate-800">{symbol}</span>
                      </td>
                      <td className="px-6 py-4">
                        <span
                          className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                            isTrade
                              ? 'bg-blue-100 text-blue-800'
                              : 'bg-green-100 text-green-800'
                          }`}
                        >
                          {event.eventType}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-right">
                        <span
                          className={`font-mono font-semibold ${
                            priceChange === 'up'
                              ? 'text-green-600'
                              : priceChange === 'down'
                              ? 'text-red-600'
                              : 'text-slate-800'
                          }`}
                        >
                          {formatPrice(getPrice(event))}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-right font-mono text-sm text-slate-600">
                        {quote ? (
                          <>
                            {formatPrice(quote.bidPrice)} / {formatPrice(quote.askPrice)}
                          </>
                        ) : (
                          '—'
                        )}
                      </td>
                      <td className="px-6 py-4 text-right font-mono text-slate-600">
                        {trade ? trade.size.toLocaleString() : quote ? `${quote.bidSize} / ${quote.askSize}` : '—'}
                      </td>
                      <td className="px-6 py-4 text-slate-500">{event.source}</td>
                      <td className="px-6 py-4 text-slate-500 text-sm">
                        {formatTime(event.timestamp)}
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

function StatCard({
  title,
  value,
  icon,
  color,
}: {
  title: string;
  value: string;
  icon: React.ReactNode;
  color: string;
}) {
  return (
    <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-6">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm text-slate-500">{title}</p>
          <p className="text-2xl font-bold text-slate-800 mt-1">{value}</p>
        </div>
        <div className={`${color} text-white p-3 rounded-lg`}>{icon}</div>
      </div>
    </div>
  );
}

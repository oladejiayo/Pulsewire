const API_BASE = '/api';

async function fetchApi<T>(endpoint: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${endpoint}`, {
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
    ...options,
  });

  if (!response.ok) {
    throw new Error(`API Error: ${response.status} ${response.statusText}`);
  }

  return response.json();
}

// Instruments API
export const instrumentsApi = {
  getAll: () => fetchApi<import('../types').Instrument[]>('/instruments'),
  getById: (id: number) => fetchApi<import('../types').Instrument>(`/instruments/${id}`),
  create: (data: Omit<import('../types').Instrument, 'id'>) =>
    fetchApi<import('../types').Instrument>('/instruments', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
  update: (id: number, data: Partial<import('../types').Instrument>) =>
    fetchApi<import('../types').Instrument>(`/instruments/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    }),
  delete: (id: number) =>
    fetchApi<void>(`/instruments/${id}`, { method: 'DELETE' }),
};

// Feeds API
export const feedsApi = {
  getAll: () => fetchApi<import('../types').Feed[]>('/feeds'),
  getById: (id: number) => fetchApi<import('../types').Feed>(`/feeds/${id}`),
  create: (data: Omit<import('../types').Feed, 'id'>) =>
    fetchApi<import('../types').Feed>('/feeds', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
  update: (id: number, data: Partial<import('../types').Feed>) =>
    fetchApi<import('../types').Feed>(`/feeds/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    }),
  delete: (id: number) =>
    fetchApi<void>(`/feeds/${id}`, { method: 'DELETE' }),
};

// Subscriptions API
export const subscriptionsApi = {
  getAll: () => fetchApi<import('../types').Subscription[]>('/subscriptions'),
  getById: (id: number) => fetchApi<import('../types').Subscription>(`/subscriptions/${id}`),
  create: (data: Omit<import('../types').Subscription, 'id'>) =>
    fetchApi<import('../types').Subscription>('/subscriptions', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
  update: (id: number, data: Partial<import('../types').Subscription>) =>
    fetchApi<import('../types').Subscription>(`/subscriptions/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    }),
  delete: (id: number) =>
    fetchApi<void>(`/subscriptions/${id}`, { method: 'DELETE' }),
};

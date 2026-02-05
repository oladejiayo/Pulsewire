import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, Pencil, Trash2, X, Check } from 'lucide-react';
import { subscriptionsApi, instrumentsApi, feedsApi } from '../services/api';
import type { Subscription } from '../types';

export default function Subscriptions() {
  const queryClient = useQueryClient();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingSubscription, setEditingSubscription] = useState<Subscription | null>(null);

  const { data: subscriptions, isLoading, error } = useQuery({
    queryKey: ['subscriptions'],
    queryFn: subscriptionsApi.getAll,
  });

  const { data: instruments } = useQuery({
    queryKey: ['instruments'],
    queryFn: instrumentsApi.getAll,
  });

  const { data: feeds } = useQuery({
    queryKey: ['feeds'],
    queryFn: feedsApi.getAll,
  });

  const createMutation = useMutation({
    mutationFn: subscriptionsApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['subscriptions'] });
      setIsModalOpen(false);
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Subscription> }) =>
      subscriptionsApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['subscriptions'] });
      setEditingSubscription(null);
      setIsModalOpen(false);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: subscriptionsApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['subscriptions'] });
    },
  });

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    const data = {
      instrumentId: parseInt(formData.get('instrumentId') as string),
      feedId: parseInt(formData.get('feedId') as string),
      priority: parseInt(formData.get('priority') as string),
      active: formData.get('active') === 'on',
    };

    if (editingSubscription) {
      updateMutation.mutate({ id: editingSubscription.id, data });
    } else {
      createMutation.mutate(data);
    }
  };

  const openEditModal = (subscription: Subscription) => {
    setEditingSubscription(subscription);
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setEditingSubscription(null);
  };

  const getInstrumentSymbol = (id: number) =>
    instruments?.find((i) => i.id === id)?.symbol || `ID:${id}`;

  const getFeedName = (id: number) =>
    feeds?.find((f) => f.id === id)?.name || `ID:${id}`;

  if (error) {
    return (
      <div className="p-8">
        <div className="bg-red-50 text-red-700 p-4 rounded-lg">
          Failed to load subscriptions. Make sure the Control Plane API is running.
        </div>
      </div>
    );
  }

  return (
    <div className="p-8">
      {/* Header */}
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold text-slate-800">Subscriptions</h1>
          <p className="text-slate-500 mt-1">Link instruments to data feeds</p>
        </div>
        <button
          onClick={() => setIsModalOpen(true)}
          className="flex items-center gap-2 px-4 py-2 bg-pulse-600 text-white rounded-lg hover:bg-pulse-700 transition-colors"
        >
          <Plus className="w-5 h-5" />
          Add Subscription
        </button>
      </div>

      {/* Table */}
      <div className="bg-white rounded-xl shadow-sm border border-slate-200">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-slate-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  ID
                </th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  Instrument
                </th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  Feed
                </th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  Priority
                </th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  Status
                </th>
                <th className="px-6 py-3 text-right text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {isLoading ? (
                <tr>
                  <td colSpan={6} className="px-6 py-12 text-center text-slate-400">
                    Loading...
                  </td>
                </tr>
              ) : subscriptions?.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-6 py-12 text-center text-slate-400">
                    No subscriptions configured. Add one to connect instruments to feeds.
                  </td>
                </tr>
              ) : (
                subscriptions?.map((sub) => (
                  <tr key={sub.id} className="hover:bg-slate-50">
                    <td className="px-6 py-4 text-slate-500 font-mono">#{sub.id}</td>
                    <td className="px-6 py-4 font-semibold text-slate-800">
                      {getInstrumentSymbol(sub.instrumentId)}
                    </td>
                    <td className="px-6 py-4 text-slate-600">{getFeedName(sub.feedId)}</td>
                    <td className="px-6 py-4">
                      <span
                        className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                          sub.priority === 1
                            ? 'bg-yellow-100 text-yellow-800'
                            : sub.priority === 2
                            ? 'bg-blue-100 text-blue-800'
                            : 'bg-slate-100 text-slate-800'
                        }`}
                      >
                        {sub.priority === 1 ? 'Primary' : sub.priority === 2 ? 'Secondary' : `P${sub.priority}`}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <span
                        className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                          sub.active
                            ? 'bg-green-100 text-green-800'
                            : 'bg-red-100 text-red-800'
                        }`}
                      >
                        {sub.active ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <button
                        onClick={() => openEditModal(sub)}
                        className="text-slate-400 hover:text-pulse-600 p-1"
                      >
                        <Pencil className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => deleteMutation.mutate(sub.id)}
                        className="text-slate-400 hover:text-red-600 p-1 ml-2"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md mx-4">
            <div className="flex items-center justify-between p-4 border-b border-slate-200">
              <h2 className="text-lg font-semibold text-slate-800">
                {editingSubscription ? 'Edit Subscription' : 'Add Subscription'}
              </h2>
              <button onClick={closeModal} className="text-slate-400 hover:text-slate-600">
                <X className="w-5 h-5" />
              </button>
            </div>
            <form onSubmit={handleSubmit} className="p-4 space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">
                  Instrument
                </label>
                <select
                  name="instrumentId"
                  defaultValue={editingSubscription?.instrumentId}
                  required
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-pulse-500 focus:border-pulse-500"
                >
                  <option value="">Select an instrument...</option>
                  {instruments?.map((instrument) => (
                    <option key={instrument.id} value={instrument.id}>
                      {instrument.symbol} - {instrument.name}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">
                  Feed
                </label>
                <select
                  name="feedId"
                  defaultValue={editingSubscription?.feedId}
                  required
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-pulse-500 focus:border-pulse-500"
                >
                  <option value="">Select a feed...</option>
                  {feeds?.map((feed) => (
                    <option key={feed.id} value={feed.id}>
                      {feed.name} ({feed.provider})
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">
                  Priority
                </label>
                <select
                  name="priority"
                  defaultValue={editingSubscription?.priority || 1}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-pulse-500 focus:border-pulse-500"
                >
                  <option value={1}>1 - Primary</option>
                  <option value={2}>2 - Secondary</option>
                  <option value={3}>3 - Tertiary</option>
                </select>
              </div>
              <div className="flex items-center gap-2">
                <input
                  type="checkbox"
                  name="active"
                  id="active"
                  defaultChecked={editingSubscription?.active ?? true}
                  className="w-4 h-4 text-pulse-600 border-slate-300 rounded focus:ring-pulse-500"
                />
                <label htmlFor="active" className="text-sm font-medium text-slate-700">
                  Active
                </label>
              </div>
              <div className="flex gap-3 pt-4">
                <button
                  type="button"
                  onClick={closeModal}
                  className="flex-1 px-4 py-2 border border-slate-300 text-slate-700 rounded-lg hover:bg-slate-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="flex-1 flex items-center justify-center gap-2 px-4 py-2 bg-pulse-600 text-white rounded-lg hover:bg-pulse-700"
                >
                  <Check className="w-4 h-4" />
                  {editingSubscription ? 'Update' : 'Create'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

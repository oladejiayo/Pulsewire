import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, Pencil, Trash2, X, Check } from 'lucide-react';
import { feedsApi } from '../services/api';
import type { Feed } from '../types';

export default function Feeds() {
  const queryClient = useQueryClient();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingFeed, setEditingFeed] = useState<Feed | null>(null);

  const { data: feeds, isLoading, error } = useQuery({
    queryKey: ['feeds'],
    queryFn: feedsApi.getAll,
  });

  const createMutation = useMutation({
    mutationFn: feedsApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['feeds'] });
      setIsModalOpen(false);
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Feed> }) =>
      feedsApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['feeds'] });
      setEditingFeed(null);
      setIsModalOpen(false);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: feedsApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['feeds'] });
    },
  });

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    const data = {
      name: formData.get('name') as string,
      provider: formData.get('provider') as string,
      protocol: formData.get('protocol') as string,
      endpoint: formData.get('endpoint') as string,
      enabled: formData.get('enabled') === 'on',
    };

    if (editingFeed) {
      updateMutation.mutate({ id: editingFeed.id, data });
    } else {
      createMutation.mutate(data);
    }
  };

  const openEditModal = (feed: Feed) => {
    setEditingFeed(feed);
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setEditingFeed(null);
  };

  if (error) {
    return (
      <div className="p-8">
        <div className="bg-red-50 text-red-700 p-4 rounded-lg">
          Failed to load feeds. Make sure the Control Plane API is running.
        </div>
      </div>
    );
  }

  return (
    <div className="p-8">
      {/* Header */}
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold text-slate-800">Feeds</h1>
          <p className="text-slate-500 mt-1">Manage market data feed sources</p>
        </div>
        <button
          onClick={() => setIsModalOpen(true)}
          className="flex items-center gap-2 px-4 py-2 bg-pulse-600 text-white rounded-lg hover:bg-pulse-700 transition-colors"
        >
          <Plus className="w-5 h-5" />
          Add Feed
        </button>
      </div>

      {/* Table */}
      <div className="bg-white rounded-xl shadow-sm border border-slate-200">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-slate-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  Name
                </th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  Provider
                </th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  Protocol
                </th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  Endpoint
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
              ) : feeds?.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-6 py-12 text-center text-slate-400">
                    No feeds configured. Add one to get started.
                  </td>
                </tr>
              ) : (
                feeds?.map((feed) => (
                  <tr key={feed.id} className="hover:bg-slate-50">
                    <td className="px-6 py-4 font-semibold text-slate-800">
                      {feed.name}
                    </td>
                    <td className="px-6 py-4 text-slate-600">{feed.provider}</td>
                    <td className="px-6 py-4">
                      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                        {feed.protocol}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-slate-500 font-mono text-sm truncate max-w-xs">
                      {feed.endpoint}
                    </td>
                    <td className="px-6 py-4">
                      <span
                        className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                          feed.enabled
                            ? 'bg-green-100 text-green-800'
                            : 'bg-red-100 text-red-800'
                        }`}
                      >
                        {feed.enabled ? 'Enabled' : 'Disabled'}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <button
                        onClick={() => openEditModal(feed)}
                        className="text-slate-400 hover:text-pulse-600 p-1"
                      >
                        <Pencil className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => deleteMutation.mutate(feed.id)}
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
                {editingFeed ? 'Edit Feed' : 'Add Feed'}
              </h2>
              <button onClick={closeModal} className="text-slate-400 hover:text-slate-600">
                <X className="w-5 h-5" />
              </button>
            </div>
            <form onSubmit={handleSubmit} className="p-4 space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">
                  Name
                </label>
                <input
                  type="text"
                  name="name"
                  defaultValue={editingFeed?.name}
                  required
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-pulse-500 focus:border-pulse-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">
                  Provider
                </label>
                <input
                  type="text"
                  name="provider"
                  defaultValue={editingFeed?.provider}
                  placeholder="e.g., Reuters, Bloomberg, Synthetic"
                  required
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-pulse-500 focus:border-pulse-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">
                  Protocol
                </label>
                <select
                  name="protocol"
                  defaultValue={editingFeed?.protocol || 'WEBSOCKET'}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-pulse-500 focus:border-pulse-500"
                >
                  <option value="WEBSOCKET">WebSocket</option>
                  <option value="FIX">FIX</option>
                  <option value="REST">REST</option>
                  <option value="KAFKA">Kafka</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">
                  Endpoint
                </label>
                <input
                  type="text"
                  name="endpoint"
                  defaultValue={editingFeed?.endpoint}
                  placeholder="e.g., wss://feed.example.com/stream"
                  required
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-pulse-500 focus:border-pulse-500"
                />
              </div>
              <div className="flex items-center gap-2">
                <input
                  type="checkbox"
                  name="enabled"
                  id="enabled"
                  defaultChecked={editingFeed?.enabled ?? true}
                  className="w-4 h-4 text-pulse-600 border-slate-300 rounded focus:ring-pulse-500"
                />
                <label htmlFor="enabled" className="text-sm font-medium text-slate-700">
                  Enabled
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
                  {editingFeed ? 'Update' : 'Create'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

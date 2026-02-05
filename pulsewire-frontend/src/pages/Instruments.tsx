import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, Pencil, Trash2, X, Check } from 'lucide-react';
import { instrumentsApi } from '../services/api';
import type { Instrument } from '../types';

export default function Instruments() {
  const queryClient = useQueryClient();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingInstrument, setEditingInstrument] = useState<Instrument | null>(null);

  const { data: instruments, isLoading, error } = useQuery({
    queryKey: ['instruments'],
    queryFn: instrumentsApi.getAll,
  });

  const createMutation = useMutation({
    mutationFn: instrumentsApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['instruments'] });
      setIsModalOpen(false);
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Instrument> }) =>
      instrumentsApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['instruments'] });
      setEditingInstrument(null);
      setIsModalOpen(false);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: instrumentsApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['instruments'] });
    },
  });

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    const data = {
      symbol: formData.get('symbol') as string,
      name: formData.get('name') as string,
      type: formData.get('type') as string,
      exchange: formData.get('exchange') as string,
      active: formData.get('active') === 'on',
    };

    if (editingInstrument) {
      updateMutation.mutate({ id: editingInstrument.id, data });
    } else {
      createMutation.mutate(data);
    }
  };

  const openEditModal = (instrument: Instrument) => {
    setEditingInstrument(instrument);
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setEditingInstrument(null);
  };

  if (error) {
    return (
      <div className="p-8">
        <div className="bg-red-50 text-red-700 p-4 rounded-lg">
          Failed to load instruments. Make sure the Control Plane API is running.
        </div>
      </div>
    );
  }

  return (
    <div className="p-8">
      {/* Header */}
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold text-slate-800">Instruments</h1>
          <p className="text-slate-500 mt-1">Manage tradeable instruments</p>
        </div>
        <button
          onClick={() => setIsModalOpen(true)}
          className="flex items-center gap-2 px-4 py-2 bg-pulse-600 text-white rounded-lg hover:bg-pulse-700 transition-colors"
        >
          <Plus className="w-5 h-5" />
          Add Instrument
        </button>
      </div>

      {/* Table */}
      <div className="bg-white rounded-xl shadow-sm border border-slate-200">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-slate-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  Symbol
                </th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  Name
                </th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  Type
                </th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  Exchange
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
              ) : instruments?.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-6 py-12 text-center text-slate-400">
                    No instruments configured. Add one to get started.
                  </td>
                </tr>
              ) : (
                instruments?.map((instrument) => (
                  <tr key={instrument.id} className="hover:bg-slate-50">
                    <td className="px-6 py-4 font-semibold text-slate-800">
                      {instrument.symbol}
                    </td>
                    <td className="px-6 py-4 text-slate-600">{instrument.name}</td>
                    <td className="px-6 py-4">
                      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-slate-100 text-slate-800">
                        {instrument.type}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-slate-600">{instrument.exchange}</td>
                    <td className="px-6 py-4">
                      <span
                        className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                          instrument.active
                            ? 'bg-green-100 text-green-800'
                            : 'bg-red-100 text-red-800'
                        }`}
                      >
                        {instrument.active ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <button
                        onClick={() => openEditModal(instrument)}
                        className="text-slate-400 hover:text-pulse-600 p-1"
                      >
                        <Pencil className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => deleteMutation.mutate(instrument.id)}
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
                {editingInstrument ? 'Edit Instrument' : 'Add Instrument'}
              </h2>
              <button onClick={closeModal} className="text-slate-400 hover:text-slate-600">
                <X className="w-5 h-5" />
              </button>
            </div>
            <form onSubmit={handleSubmit} className="p-4 space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">
                  Symbol
                </label>
                <input
                  type="text"
                  name="symbol"
                  defaultValue={editingInstrument?.symbol}
                  required
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-pulse-500 focus:border-pulse-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">
                  Name
                </label>
                <input
                  type="text"
                  name="name"
                  defaultValue={editingInstrument?.name}
                  required
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-pulse-500 focus:border-pulse-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">
                  Type
                </label>
                <select
                  name="type"
                  defaultValue={editingInstrument?.type || 'EQUITY'}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-pulse-500 focus:border-pulse-500"
                >
                  <option value="EQUITY">Equity</option>
                  <option value="OPTION">Option</option>
                  <option value="FUTURE">Future</option>
                  <option value="FOREX">Forex</option>
                  <option value="CRYPTO">Crypto</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">
                  Exchange
                </label>
                <input
                  type="text"
                  name="exchange"
                  defaultValue={editingInstrument?.exchange}
                  required
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-pulse-500 focus:border-pulse-500"
                />
              </div>
              <div className="flex items-center gap-2">
                <input
                  type="checkbox"
                  name="active"
                  id="active"
                  defaultChecked={editingInstrument?.active ?? true}
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
                  {editingInstrument ? 'Update' : 'Create'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

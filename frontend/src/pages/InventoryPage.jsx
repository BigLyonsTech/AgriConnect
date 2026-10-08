import { useEffect, useState } from 'react';
import AppLayout from '../components/AppLayout';
import { listFarms } from '../api/farmApi';
import { createInventoryItem, deleteInventoryItem, listInventory } from '../api/inventoryApi';
import { extractErrorMessage } from '../utils/errors';

export default function InventoryPage() {
  const [farms, setFarms] = useState([]);
  const [items, setItems] = useState([]);
  const [form, setForm] = useState({ farmId: '', itemName: '', category: '', quantityKg: '' });
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const load = () => {
    listInventory().then(setItems).catch((err) => setError(extractErrorMessage(err)));
  };

  useEffect(() => {
    listFarms().then(setFarms).catch(() => {});
    load();
  }, []);

  const handleChange = (e) => setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      await createInventoryItem({ ...form, farmId: Number(form.farmId), quantityKg: Number(form.quantityKg) });
      setForm({ farmId: '', itemName: '', category: '', quantityKg: '' });
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id) => {
    try {
      await deleteInventoryItem(id);
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  return (
    <AppLayout>
      <div className="page-header"><h1>Inventory</h1></div>
      {error && <div className="page-error">{error}</div>}

      <div className="form-card">
        <h3>Add inventory item</h3>
        {farms.length === 0 ? (
          <p className="page-empty">Add a farm first before recording inventory.</p>
        ) : (
          <form onSubmit={handleSubmit}>
            <div className="form-grid">
              <label>
                Farm
                <select name="farmId" value={form.farmId} onChange={handleChange} required>
                  <option value="">Select a farm</option>
                  {farms.map((f) => <option key={f.id} value={f.id}>{f.name}</option>)}
                </select>
              </label>
              <label>
                Item name
                <input name="itemName" value={form.itemName} onChange={handleChange} required placeholder="Tomatoes" />
              </label>
              <label>
                Category
                <input name="category" value={form.category} onChange={handleChange} required placeholder="Produce" />
              </label>
              <label>
                Quantity (kg)
                <input name="quantityKg" type="number" step="0.1" value={form.quantityKg} onChange={handleChange} required />
              </label>
            </div>
            <button type="submit" className="btn-primary-sm" disabled={submitting}>
              {submitting ? 'Adding…' : 'Add Item'}
            </button>
          </form>
        )}
      </div>

      {items.length === 0 ? (
        <p className="page-empty">No inventory recorded yet.</p>
      ) : (
        <table className="data-table">
          <thead><tr><th>Item</th><th>Category</th><th>Quantity (kg)</th><th></th></tr></thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.id}>
                <td>{item.itemName}</td>
                <td>{item.category}</td>
                <td>{item.quantityKg}</td>
                <td><button className="btn-danger-sm" onClick={() => handleDelete(item.id)}>Delete</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </AppLayout>
  );
}

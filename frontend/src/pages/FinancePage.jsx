import { useEffect, useState } from 'react';
import AppLayout from '../components/AppLayout';
import { listFarms } from '../api/farmApi';
import { createExpense, createRevenue, deleteExpense, deleteRevenue, listExpenses, listRevenues } from '../api/financeApi';
import { extractErrorMessage } from '../utils/errors';

export default function FinancePage() {
  const [farms, setFarms] = useState([]);
  const [expenses, setExpenses] = useState([]);
  const [revenues, setRevenues] = useState([]);
  const [error, setError] = useState('');

  const [expenseForm, setExpenseForm] = useState({ farmId: '', category: '', amount: '', description: '', date: '' });
  const [revenueForm, setRevenueForm] = useState({ farmId: '', source: '', amount: '', date: '' });

  const loadAll = () => {
    listExpenses().then(setExpenses).catch((err) => setError(extractErrorMessage(err)));
    listRevenues().then(setRevenues).catch((err) => setError(extractErrorMessage(err)));
  };

  useEffect(() => {
    listFarms().then(setFarms).catch(() => {});
    loadAll();
  }, []);

  const totalExpenses = expenses.reduce((sum, e) => sum + Number(e.amount), 0);
  const totalRevenue = revenues.reduce((sum, r) => sum + Number(r.amount), 0);

  const handleExpenseSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await createExpense({ ...expenseForm, farmId: Number(expenseForm.farmId), amount: Number(expenseForm.amount) });
      setExpenseForm({ farmId: '', category: '', amount: '', description: '', date: '' });
      loadAll();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  const handleRevenueSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await createRevenue({ ...revenueForm, farmId: Number(revenueForm.farmId), amount: Number(revenueForm.amount) });
      setRevenueForm({ farmId: '', source: '', amount: '', date: '' });
      loadAll();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  return (
    <AppLayout>
      <div className="page-header"><h1>Finance</h1></div>
      {error && <div className="page-error">{error}</div>}

      <div className="summary-grid">
        <div className="summary-card"><small>Total Revenue</small><strong>₦{totalRevenue.toLocaleString()}</strong></div>
        <div className="summary-card"><small>Total Expenses</small><strong>₦{totalExpenses.toLocaleString()}</strong></div>
        <div className="summary-card profit"><small>Net Profit</small><strong>₦{(totalRevenue - totalExpenses).toLocaleString()}</strong></div>
      </div>

      {farms.length === 0 ? (
        <p className="page-empty">Add a farm first before recording finances.</p>
      ) : (
        <>
          <div className="form-card">
            <h3>Record an expense</h3>
            <form onSubmit={handleExpenseSubmit}>
              <div className="form-grid">
                <label>
                  Farm
                  <select value={expenseForm.farmId} onChange={(e) => setExpenseForm((p) => ({ ...p, farmId: e.target.value }))} required>
                    <option value="">Select a farm</option>
                    {farms.map((f) => <option key={f.id} value={f.id}>{f.name}</option>)}
                  </select>
                </label>
                <label>
                  Category
                  <input value={expenseForm.category} onChange={(e) => setExpenseForm((p) => ({ ...p, category: e.target.value }))} required placeholder="Seeds" />
                </label>
                <label>
                  Amount (₦)
                  <input type="number" step="0.01" value={expenseForm.amount} onChange={(e) => setExpenseForm((p) => ({ ...p, amount: e.target.value }))} required />
                </label>
                <label>
                  Date
                  <input type="date" value={expenseForm.date} onChange={(e) => setExpenseForm((p) => ({ ...p, date: e.target.value }))} required />
                </label>
                <label>
                  Description
                  <input value={expenseForm.description} onChange={(e) => setExpenseForm((p) => ({ ...p, description: e.target.value }))} placeholder="Optional" />
                </label>
              </div>
              <button type="submit" className="btn-primary-sm">Record Expense</button>
            </form>
          </div>

          {expenses.length > 0 && (
            <table className="data-table" style={{ marginBottom: 32 }}>
              <thead><tr><th>Category</th><th>Amount</th><th>Date</th><th>Description</th><th></th></tr></thead>
              <tbody>
                {expenses.map((e) => (
                  <tr key={e.id}>
                    <td>{e.category}</td>
                    <td>₦{Number(e.amount).toLocaleString()}</td>
                    <td>{e.date}</td>
                    <td>{e.description || '—'}</td>
                    <td><button className="btn-danger-sm" onClick={() => deleteExpense(e.id).then(loadAll)}>Delete</button></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}

          <div className="form-card">
            <h3>Record revenue</h3>
            <form onSubmit={handleRevenueSubmit}>
              <div className="form-grid">
                <label>
                  Farm
                  <select value={revenueForm.farmId} onChange={(e) => setRevenueForm((p) => ({ ...p, farmId: e.target.value }))} required>
                    <option value="">Select a farm</option>
                    {farms.map((f) => <option key={f.id} value={f.id}>{f.name}</option>)}
                  </select>
                </label>
                <label>
                  Source
                  <input value={revenueForm.source} onChange={(e) => setRevenueForm((p) => ({ ...p, source: e.target.value }))} required placeholder="Marketplace sale" />
                </label>
                <label>
                  Amount (₦)
                  <input type="number" step="0.01" value={revenueForm.amount} onChange={(e) => setRevenueForm((p) => ({ ...p, amount: e.target.value }))} required />
                </label>
                <label>
                  Date
                  <input type="date" value={revenueForm.date} onChange={(e) => setRevenueForm((p) => ({ ...p, date: e.target.value }))} required />
                </label>
              </div>
              <button type="submit" className="btn-primary-sm">Record Revenue</button>
            </form>
          </div>

          {revenues.length > 0 && (
            <table className="data-table">
              <thead><tr><th>Source</th><th>Amount</th><th>Date</th><th></th></tr></thead>
              <tbody>
                {revenues.map((r) => (
                  <tr key={r.id}>
                    <td>{r.source}</td>
                    <td>₦{Number(r.amount).toLocaleString()}</td>
                    <td>{r.date}</td>
                    <td><button className="btn-danger-sm" onClick={() => deleteRevenue(r.id).then(loadAll)}>Delete</button></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}
    </AppLayout>
  );
}

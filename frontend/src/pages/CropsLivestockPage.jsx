import { useEffect, useState } from 'react';
import AppLayout from '../components/AppLayout';
import { listFarms, listCrops, createCrop, deleteCrop, listLivestock, createLivestock, deleteLivestock } from '../api/farmApi';
import { extractErrorMessage } from '../utils/errors';

const CROP_STATUSES = ['PLANTED', 'GROWING', 'HARVESTED', 'FAILED'];

export default function CropsLivestockPage() {
  const [farms, setFarms] = useState([]);
  const [crops, setCrops] = useState([]);
  const [livestock, setLivestock] = useState([]);
  const [error, setError] = useState('');

  const [cropForm, setCropForm] = useState({ farmId: '', cropName: '', plantingDate: '', expectedHarvestDate: '', status: 'PLANTED' });
  const [stockForm, setStockForm] = useState({ farmId: '', species: '', breed: '', quantity: '', healthNotes: '' });
  const [submittingCrop, setSubmittingCrop] = useState(false);
  const [submittingStock, setSubmittingStock] = useState(false);

  const loadAll = () => {
    listCrops().then(setCrops).catch((err) => setError(extractErrorMessage(err)));
    listLivestock().then(setLivestock).catch((err) => setError(extractErrorMessage(err)));
  };

  useEffect(() => {
    listFarms().then(setFarms).catch(() => {});
    loadAll();
  }, []);

  const handleCropSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmittingCrop(true);
    try {
      await createCrop({ ...cropForm, farmId: Number(cropForm.farmId) });
      setCropForm({ farmId: '', cropName: '', plantingDate: '', expectedHarvestDate: '', status: 'PLANTED' });
      loadAll();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmittingCrop(false);
    }
  };

  const handleStockSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmittingStock(true);
    try {
      await createLivestock({ ...stockForm, farmId: Number(stockForm.farmId), quantity: Number(stockForm.quantity) });
      setStockForm({ farmId: '', species: '', breed: '', quantity: '', healthNotes: '' });
      loadAll();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmittingStock(false);
    }
  };

  return (
    <AppLayout>
      <div className="page-header"><h1>Crops &amp; Livestock</h1></div>
      {error && <div className="page-error">{error}</div>}

      {farms.length === 0 ? (
        <p className="page-empty">Add a farm first (see the Farms tab) before recording crops or livestock.</p>
      ) : (
        <>
          <div className="form-card">
            <h3>Add a crop</h3>
            <form onSubmit={handleCropSubmit}>
              <div className="form-grid">
                <label>
                  Farm
                  <select value={cropForm.farmId} onChange={(e) => setCropForm((p) => ({ ...p, farmId: e.target.value }))} required>
                    <option value="">Select a farm</option>
                    {farms.map((f) => <option key={f.id} value={f.id}>{f.name}</option>)}
                  </select>
                </label>
                <label>
                  Crop name
                  <input value={cropForm.cropName} onChange={(e) => setCropForm((p) => ({ ...p, cropName: e.target.value }))} required placeholder="Maize" />
                </label>
                <label>
                  Planting date
                  <input type="date" value={cropForm.plantingDate} onChange={(e) => setCropForm((p) => ({ ...p, plantingDate: e.target.value }))} />
                </label>
                <label>
                  Expected harvest
                  <input type="date" value={cropForm.expectedHarvestDate} onChange={(e) => setCropForm((p) => ({ ...p, expectedHarvestDate: e.target.value }))} />
                </label>
                <label>
                  Status
                  <select value={cropForm.status} onChange={(e) => setCropForm((p) => ({ ...p, status: e.target.value }))}>
                    {CROP_STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
                  </select>
                </label>
              </div>
              <button type="submit" className="btn-primary-sm" disabled={submittingCrop}>
                {submittingCrop ? 'Adding…' : 'Add Crop'}
              </button>
            </form>
          </div>

          {crops.length === 0 ? (
            <p className="page-empty">No crops recorded yet.</p>
          ) : (
            <table className="data-table" style={{ marginBottom: 32 }}>
              <thead><tr><th>Crop</th><th>Farm</th><th>Planted</th><th>Expected Harvest</th><th>Status</th><th></th></tr></thead>
              <tbody>
                {crops.map((c) => (
                  <tr key={c.id}>
                    <td>{c.cropName}</td>
                    <td>{c.farmName}</td>
                    <td>{c.plantingDate || '—'}</td>
                    <td>{c.expectedHarvestDate || '—'}</td>
                    <td><span className={`badge badge-${c.status.toLowerCase()}`}>{c.status}</span></td>
                    <td><button className="btn-danger-sm" onClick={() => deleteCrop(c.id).then(loadAll)}>Delete</button></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}

          <div className="form-card">
            <h3>Add livestock</h3>
            <form onSubmit={handleStockSubmit}>
              <div className="form-grid">
                <label>
                  Farm
                  <select value={stockForm.farmId} onChange={(e) => setStockForm((p) => ({ ...p, farmId: e.target.value }))} required>
                    <option value="">Select a farm</option>
                    {farms.map((f) => <option key={f.id} value={f.id}>{f.name}</option>)}
                  </select>
                </label>
                <label>
                  Species
                  <input value={stockForm.species} onChange={(e) => setStockForm((p) => ({ ...p, species: e.target.value }))} required placeholder="Poultry" />
                </label>
                <label>
                  Breed
                  <input value={stockForm.breed} onChange={(e) => setStockForm((p) => ({ ...p, breed: e.target.value }))} placeholder="Broiler" />
                </label>
                <label>
                  Quantity
                  <input type="number" value={stockForm.quantity} onChange={(e) => setStockForm((p) => ({ ...p, quantity: e.target.value }))} required />
                </label>
                <label>
                  Health notes
                  <input value={stockForm.healthNotes} onChange={(e) => setStockForm((p) => ({ ...p, healthNotes: e.target.value }))} placeholder="All vaccinated" />
                </label>
              </div>
              <button type="submit" className="btn-primary-sm" disabled={submittingStock}>
                {submittingStock ? 'Adding…' : 'Add Livestock'}
              </button>
            </form>
          </div>

          {livestock.length === 0 ? (
            <p className="page-empty">No livestock recorded yet.</p>
          ) : (
            <table className="data-table">
              <thead><tr><th>Species</th><th>Breed</th><th>Quantity</th><th>Health Notes</th><th></th></tr></thead>
              <tbody>
                {livestock.map((l) => (
                  <tr key={l.id}>
                    <td>{l.species}</td>
                    <td>{l.breed || '—'}</td>
                    <td>{l.quantity}</td>
                    <td>{l.healthNotes || '—'}</td>
                    <td><button className="btn-danger-sm" onClick={() => deleteLivestock(l.id).then(loadAll)}>Delete</button></td>
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

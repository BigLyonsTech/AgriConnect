import { useEffect, useState } from 'react';
import AppLayout from '../components/AppLayout';
import { createFarm, deleteFarm, listFarms, getFarmWeather } from '../api/farmApi';
import { extractErrorMessage } from '../utils/errors';

export default function FarmsPage() {
  const [farms, setFarms] = useState([]);
  const [form, setForm] = useState({ name: '', location: '', sizeHectares: '' });
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [weatherByFarm, setWeatherByFarm] = useState({});

  const load = () => {
    listFarms().then(setFarms).catch((err) => setError(extractErrorMessage(err)));
  };

  useEffect(load, []);

  const handleChange = (e) => setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      await createFarm({ ...form, sizeHectares: Number(form.sizeHectares) });
      setForm({ name: '', location: '', sizeHectares: '' });
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id) => {
    try {
      await deleteFarm(id);
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  const handleCheckWeather = async (farm) => {
    try {
      const data = await getFarmWeather(farm.id);
      setWeatherByFarm((prev) => ({ ...prev, [farm.id]: data }));
    } catch (err) {
      setWeatherByFarm((prev) => ({ ...prev, [farm.id]: { error: extractErrorMessage(err, 'Weather lookup failed.') } }));
    }
  };

  return (
    <AppLayout>
      <div className="page-header">
        <h1>Farms</h1>
      </div>

      {error && <div className="page-error">{error}</div>}

      <div className="form-card">
        <h3>Add a farm</h3>
        <form onSubmit={handleSubmit}>
          <div className="form-grid">
            <label>
              Name
              <input name="name" value={form.name} onChange={handleChange} required placeholder="Green Valley Farm" />
            </label>
            <label>
              Location (city)
              <input name="location" value={form.location} onChange={handleChange} required placeholder="Lagos" />
            </label>
            <label>
              Size (hectares)
              <input name="sizeHectares" type="number" step="0.1" value={form.sizeHectares} onChange={handleChange} required />
            </label>
          </div>
          <button type="submit" className="btn-primary-sm" disabled={submitting}>
            {submitting ? 'Adding…' : 'Add Farm'}
          </button>
        </form>
      </div>

      {farms.length === 0 ? (
        <p className="page-empty">No farms yet — add your first one above.</p>
      ) : (
        <table className="data-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Location</th>
              <th>Size (ha)</th>
              <th>Weather</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {farms.map((farm) => (
              <tr key={farm.id}>
                <td>{farm.name}</td>
                <td>{farm.location}</td>
                <td>{farm.sizeHectares}</td>
                <td>
                  {weatherByFarm[farm.id] ? (
                    weatherByFarm[farm.id].error ? (
                      <span style={{ color: '#b3261e', fontSize: 12 }}>{weatherByFarm[farm.id].error}</span>
                    ) : (
                      <span>
                        🌤️ {weatherByFarm[farm.id].temperatureCelsius}°C, {weatherByFarm[farm.id].description}
                      </span>
                    )
                  ) : (
                    <button className="btn-danger-sm" style={{ color: '#08783f', borderColor: '#c8e6d3' }} onClick={() => handleCheckWeather(farm)}>
                      Check weather
                    </button>
                  )}
                </td>
                <td>
                  <button className="btn-danger-sm" onClick={() => handleDelete(farm.id)}>Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </AppLayout>
  );
}

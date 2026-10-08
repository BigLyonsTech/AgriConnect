import { useEffect, useState } from 'react';
import AppLayout from '../components/AppLayout';
import { useAuth } from '../features/auth/AuthContext';
import { listFarms } from '../api/farmApi';
import { browseListings, closeListing, createListing, myListings, placeOrder } from '../api/marketplaceApi';
import { extractErrorMessage } from '../utils/errors';

const CAN_SELL = ['OWNER', 'ADMIN', 'FARMER'];

export default function MarketplacePage() {
  const { user } = useAuth();
  const [tab, setTab] = useState('browse');
  const [farms, setFarms] = useState([]);
  const [active, setActive] = useState([]);
  const [mine, setMine] = useState([]);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  const [listingForm, setListingForm] = useState({ farmId: '', produceName: '', description: '', pricePerUnit: '', unit: 'kg', quantityAvailable: '' });
  const [orderQuantities, setOrderQuantities] = useState({});

  const loadBrowse = () => browseListings().then(setActive).catch((err) => setError(extractErrorMessage(err)));
  const loadMine = () => myListings().then(setMine).catch((err) => setError(extractErrorMessage(err)));

  useEffect(() => {
    loadBrowse();
    if (CAN_SELL.includes(user?.role)) {
      listFarms().then(setFarms).catch(() => {});
      loadMine();
    }
  }, [user?.role]);

  const handleCreateListing = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await createListing({
        ...listingForm,
        farmId: listingForm.farmId ? Number(listingForm.farmId) : null,
        pricePerUnit: Number(listingForm.pricePerUnit),
        quantityAvailable: Number(listingForm.quantityAvailable),
      });
      setListingForm({ farmId: '', produceName: '', description: '', pricePerUnit: '', unit: 'kg', quantityAvailable: '' });
      loadMine();
      loadBrowse();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  const handleClose = async (id) => {
    try {
      await closeListing(id);
      loadMine();
      loadBrowse();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  const handlePlaceOrder = async (listing) => {
    setError('');
    setNotice('');
    const quantity = Number(orderQuantities[listing.id] || 0);
    if (!quantity || quantity <= 0) {
      setError('Enter a quantity to order first.');
      return;
    }
    try {
      await placeOrder({ listingId: listing.id, quantity });
      setNotice(`Order placed for ${quantity} ${listing.unit} of ${listing.produceName}. Check the Orders tab to pay.`);
      loadBrowse();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  return (
    <AppLayout>
      <div className="page-header"><h1>Marketplace</h1></div>
      {error && <div className="page-error">{error}</div>}
      {notice && <div className="page-error" style={{ background: '#eaf7ef', color: '#08783f' }}>{notice}</div>}

      <div style={{ display: 'flex', gap: 8, marginBottom: 20 }}>
        <button className="btn-primary-sm" style={tab !== 'browse' ? { background: '#fff', color: '#08783f', border: '1px solid #c8e6d3' } : {}} onClick={() => setTab('browse')}>
          Browse
        </button>
        {CAN_SELL.includes(user?.role) && (
          <button className="btn-primary-sm" style={tab !== 'mine' ? { background: '#fff', color: '#08783f', border: '1px solid #c8e6d3' } : {}} onClick={() => setTab('mine')}>
            My Listings
          </button>
        )}
      </div>

      {tab === 'browse' && (
        active.length === 0 ? (
          <p className="page-empty">No active listings right now.</p>
        ) : (
          <table className="data-table">
            <thead><tr><th>Produce</th><th>Price</th><th>Available</th><th>Order</th></tr></thead>
            <tbody>
              {active.map((listing) => (
                <tr key={listing.id}>
                  <td>
                    <strong>{listing.produceName}</strong>
                    {listing.description && <div style={{ fontSize: 12, color: '#94a49d' }}>{listing.description}</div>}
                  </td>
                  <td>₦{Number(listing.pricePerUnit).toLocaleString()} / {listing.unit}</td>
                  <td>{listing.quantityAvailable} {listing.unit}</td>
                  <td>
                    {user?.role === 'BUYER' ? (
                      <div style={{ display: 'flex', gap: 6 }}>
                        <input
                          type="number"
                          min="1"
                          placeholder="Qty"
                          style={{ width: 70, padding: '6px 8px', border: '1px solid #dfe9e4', borderRadius: 6 }}
                          value={orderQuantities[listing.id] || ''}
                          onChange={(e) => setOrderQuantities((p) => ({ ...p, [listing.id]: e.target.value }))}
                        />
                        <button className="btn-primary-sm" onClick={() => handlePlaceOrder(listing)}>Order</button>
                      </div>
                    ) : (
                      <span style={{ fontSize: 12, color: '#94a49d' }}>Only BUYER accounts can order</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )
      )}

      {tab === 'mine' && CAN_SELL.includes(user?.role) && (
        <>
          <div className="form-card">
            <h3>Create a listing</h3>
            <form onSubmit={handleCreateListing}>
              <div className="form-grid">
                <label>
                  Farm (optional)
                  <select value={listingForm.farmId} onChange={(e) => setListingForm((p) => ({ ...p, farmId: e.target.value }))}>
                    <option value="">None</option>
                    {farms.map((f) => <option key={f.id} value={f.id}>{f.name}</option>)}
                  </select>
                </label>
                <label>
                  Produce name
                  <input value={listingForm.produceName} onChange={(e) => setListingForm((p) => ({ ...p, produceName: e.target.value }))} required placeholder="Tomatoes" />
                </label>
                <label>
                  Price per unit (₦)
                  <input type="number" step="0.01" value={listingForm.pricePerUnit} onChange={(e) => setListingForm((p) => ({ ...p, pricePerUnit: e.target.value }))} required />
                </label>
                <label>
                  Unit
                  <input value={listingForm.unit} onChange={(e) => setListingForm((p) => ({ ...p, unit: e.target.value }))} required placeholder="kg" />
                </label>
                <label>
                  Quantity available
                  <input type="number" step="0.1" value={listingForm.quantityAvailable} onChange={(e) => setListingForm((p) => ({ ...p, quantityAvailable: e.target.value }))} required />
                </label>
                <label>
                  Description
                  <input value={listingForm.description} onChange={(e) => setListingForm((p) => ({ ...p, description: e.target.value }))} placeholder="Optional" />
                </label>
              </div>
              <button type="submit" className="btn-primary-sm">Create Listing</button>
            </form>
          </div>

          {mine.length === 0 ? (
            <p className="page-empty">You haven't listed anything yet.</p>
          ) : (
            <table className="data-table">
              <thead><tr><th>Produce</th><th>Price</th><th>Available</th><th>Status</th><th></th></tr></thead>
              <tbody>
                {mine.map((listing) => (
                  <tr key={listing.id}>
                    <td>{listing.produceName}</td>
                    <td>₦{Number(listing.pricePerUnit).toLocaleString()} / {listing.unit}</td>
                    <td>{listing.quantityAvailable} {listing.unit}</td>
                    <td><span className={`badge badge-${listing.status.toLowerCase()}`}>{listing.status}</span></td>
                    <td>
                      {listing.status === 'ACTIVE' && (
                        <button className="btn-danger-sm" onClick={() => handleClose(listing.id)}>Close</button>
                      )}
                    </td>
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

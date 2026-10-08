import { useEffect, useState } from 'react';
import AppLayout from '../components/AppLayout';
import { useAuth } from '../features/auth/AuthContext';
import { markOrderFulfilled, myOrders, ordersAgainstMyListings } from '../api/marketplaceApi';
import { initiatePayment, releaseFunds } from '../api/paymentApi';
import { extractErrorMessage } from '../utils/errors';

const CAN_SELL = ['OWNER', 'ADMIN', 'FARMER'];

export default function OrdersPage() {
  const { user } = useAuth();
  const [asBuyer, setAsBuyer] = useState([]);
  const [asSeller, setAsSeller] = useState([]);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  const loadBuyer = () => myOrders().then(setAsBuyer).catch(() => {});
  const loadSeller = () => ordersAgainstMyListings().then(setAsSeller).catch(() => {});

  useEffect(() => {
    if (user?.role === 'BUYER') loadBuyer();
    if (CAN_SELL.includes(user?.role)) loadSeller();
  }, [user?.role]);

  const handlePay = async (order) => {
    setError('');
    setNotice('');
    try {
      const result = await initiatePayment({ orderId: order.id, payerEmail: user.email });
      setNotice(
        `Payment initialized (ref: ${result.reference}). In production you'd be redirected to: ${result.authorizationUrl}`
      );
      loadBuyer();
    } catch (err) {
      setError(extractErrorMessage(err, 'Could not start payment. Check that PAYSTACK_SECRET_KEY is configured on the backend.'));
    }
  };

  const handleFulfill = async (order) => {
    setError('');
    try {
      await markOrderFulfilled(order.id);
      loadSeller();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  const handleRelease = async (order) => {
    setError('');
    try {
      await releaseFunds(order.id);
      setNotice('Funds released to seller.');
      loadSeller();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  return (
    <AppLayout>
      <div className="page-header"><h1>Orders</h1></div>
      {error && <div className="page-error">{error}</div>}
      {notice && <div className="page-error" style={{ background: '#eaf7ef', color: '#08783f' }}>{notice}</div>}

      {user?.role === 'BUYER' && (
        <>
          <h3 style={{ marginBottom: 12, fontSize: 15 }}>My Purchases</h3>
          {asBuyer.length === 0 ? (
            <p className="page-empty">You haven't placed any orders yet — visit the Marketplace tab.</p>
          ) : (
            <table className="data-table" style={{ marginBottom: 32 }}>
              <thead><tr><th>Order #</th><th>Quantity</th><th>Total</th><th>Status</th><th></th></tr></thead>
              <tbody>
                {asBuyer.map((order) => (
                  <tr key={order.id}>
                    <td>#{order.id}</td>
                    <td>{order.quantity}</td>
                    <td>₦{Number(order.totalAmount).toLocaleString()}</td>
                    <td><span className={`badge badge-${order.status.toLowerCase()}`}>{order.status}</span></td>
                    <td>
                      {order.status === 'PENDING_PAYMENT' && (
                        <button className="btn-primary-sm" onClick={() => handlePay(order)}>Pay with Paystack</button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}

      {CAN_SELL.includes(user?.role) && (
        <>
          <h3 style={{ marginBottom: 12, fontSize: 15 }}>Orders Against My Listings</h3>
          {asSeller.length === 0 ? (
            <p className="page-empty">No orders against your listings yet.</p>
          ) : (
            <table className="data-table">
              <thead><tr><th>Order #</th><th>Quantity</th><th>Total</th><th>Status</th><th></th></tr></thead>
              <tbody>
                {asSeller.map((order) => (
                  <tr key={order.id}>
                    <td>#{order.id}</td>
                    <td>{order.quantity}</td>
                    <td>₦{Number(order.totalAmount).toLocaleString()}</td>
                    <td><span className={`badge badge-${order.status.toLowerCase()}`}>{order.status}</span></td>
                    <td style={{ display: 'flex', gap: 6 }}>
                      {order.status === 'PAID' && (
                        <button className="btn-primary-sm" onClick={() => handleFulfill(order)}>Mark Fulfilled</button>
                      )}
                      {order.status === 'PAID' && (
                        <button className="btn-danger-sm" style={{ color: '#08783f', borderColor: '#c8e6d3' }} onClick={() => handleRelease(order)}>
                          Release Funds
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}

      {user?.role !== 'BUYER' && !CAN_SELL.includes(user?.role) && (
        <p className="page-empty">Your role doesn't have order visibility.</p>
      )}
    </AppLayout>
  );
}

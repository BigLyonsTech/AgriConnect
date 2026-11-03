import { Component } from 'react';
import '../styles/errorboundary.css';

/**
 * Last line of defence for the UI: if any page throws while rendering,
 * show a friendly fallback instead of a blank screen. API errors are
 * handled per page with extractErrorMessage; this only catches crashes.
 */
export default class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  componentDidCatch(error, info) {
    console.error('[AgriConnect] UI crashed:', error, info?.componentStack);
  }

  render() {
    if (!this.state.hasError) return this.props.children;
    return (
      <div className="error-boundary" role="alert">
        <div className="error-boundary-card">
          <h1>Something went wrong</h1>
          <p>This page hit an unexpected problem. Your data is safe. Try reloading, or go back to the start.</p>
          <div className="error-boundary-actions">
            <button type="button" className="error-boundary-primary" onClick={() => window.location.reload()}>
              Reload page
            </button>
            <a href="/" className="error-boundary-secondary">Go to home</a>
          </div>
        </div>
      </div>
    );
  }
}

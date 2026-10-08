import { useState } from 'react';
import { Link } from 'react-router-dom';
import '../styles/landing.css';

export default function LandingPage() {
  const [menuOpen, setMenuOpen] = useState(false);

  return (
    <div className="landing">
      <header>
        <div className="container nav">
          <Link to="/" className="logo">
            <div className="logo-icon">🌱</div>
            <div>
              <h2>Agri<span>Connect</span></h2>
              <small>Farm Smarter. Grow Together.</small>
            </div>
          </Link>

          <nav className="nav-links">
            <a href="#home">Home</a>
            <a href="#features">Features</a>
            <a href="#about">About</a>
            <a href="#contact">Contact</a>
          </nav>

          <div className="nav-actions">
            <Link to="/login" className="btn btn-outline">Log In</Link>
            <Link to="/register" className="btn btn-primary">Get Started</Link>
          </div>
          <button className="menu" onClick={() => setMenuOpen((open) => !open)} aria-label="Toggle menu">
            ☰
          </button>
        </div>

        {menuOpen && (
          <div className="container mobile-menu open">
            <a href="#home" onClick={() => setMenuOpen(false)}>Home</a>
            <a href="#features" onClick={() => setMenuOpen(false)}>Features</a>
            <a href="#about" onClick={() => setMenuOpen(false)}>About</a>
            <a href="#contact" onClick={() => setMenuOpen(false)}>Contact</a>
            <Link to="/login" onClick={() => setMenuOpen(false)}>Log In</Link>
            <Link to="/register" className="btn btn-primary" onClick={() => setMenuOpen(false)}>Get Started</Link>
          </div>
        )}
      </header>

      <main>
        <section className="hero" id="home">
          <div className="container hero-grid">
            <div>
              <div className="badge">🌿 Modern Farm Management &amp; Marketplace</div>
              <h1>Manage Your Farm.<br />Sell Your Produce.<br /><span>Grow Your Business.</span></h1>
              <p>
                AgriConnect is a complete farm management and produce marketplace
                platform for farmers, agribusinesses, cooperatives and buyers.
                Track your farm, manage inventory, connect with buyers and grow.
              </p>

              <div className="hero-buttons">
                <Link to="/register" className="btn btn-primary">Get Started Free →</Link>
                <a href="#features" className="btn btn-outline">▶ Watch Demo</a>
              </div>

              <div className="checks">
                <span>Multi-tenant</span>
                <span>Secure &amp; Reliable</span>
                <span>Easy to Use</span>
              </div>
            </div>

            <div className="visual">
              <img
                className="hero-image"
                src="https://images.unsplash.com/photo-1625246333195-78d9c38ad449?auto=format&fit=crop&w=1000&q=85"
                alt="Farmer working on a farm"
              />

              <div className="floating weather">
                <small>📍 Lagos, NG</small>
                <strong>🌤️ 28°C</strong>
                <small>Partly Cloudy</small>
              </div>

              <div className="floating overview">
                <h4>Farm Overview</h4>
                <div className="stats">
                  <div className="stat"><small>Total Farms</small><b>12</b></div>
                  <div className="stat"><small>Active Crops</small><b>38</b></div>
                  <div className="stat"><small>Inventory</small><b>4,820kg</b></div>
                  <div className="stat"><small>Revenue</small><b>₦2.4M</b></div>
                </div>
              </div>

              <div className="floating product">
                <img src="https://images.unsplash.com/photo-1546470427-227c8e3f4a80?auto=format&fit=crop&w=500&q=80" alt="Fresh tomatoes" />
                <h5>Fresh Tomatoes</h5>
                <p>₦2,500 / kg</p>
                <span className="buy">Buy Now</span>
              </div>
            </div>
          </div>
        </section>

        <section className="features" id="features">
          <div className="container feature-grid">
            <article className="feature">
              <div className="feature-icon">🌱</div>
              <h3>Farm Management</h3>
              <p>Track crops, livestock and farm activities with ease.</p>
            </article>
            <article className="feature">
              <div className="feature-icon">🛒</div>
              <h3>Produce Marketplace</h3>
              <p>Buy and sell fresh produce directly from farmers.</p>
            </article>
            <article className="feature">
              <div className="feature-icon">💳</div>
              <h3>Secure Payments</h3>
              <p>Safe and easy transactions with payment integration.</p>
            </article>
            <article className="feature">
              <div className="feature-icon">☁️</div>
              <h3>Weather Insights</h3>
              <p>Get useful weather updates for better decisions.</p>
            </article>
            <article className="feature">
              <div className="feature-icon">📈</div>
              <h3>Analytics &amp; Reports</h3>
              <p>Monitor performance and grow your profits.</p>
            </article>
            <article className="feature">
              <div className="feature-icon">🛡️</div>
              <h3>Multi-Tenant Support</h3>
              <p>Built for farms, cooperatives and agribusinesses.</p>
            </article>
          </div>
        </section>

        <section className="why" id="about">
          <div className="container why-grid">
            <div>
              <div className="eyebrow">WHY AGRICONNECT?</div>
              <h2>Built for a Stronger Agriculture Future</h2>
              <p>
                We're not just a platform — we're a partner in your growth.
                From the farm to the market, AgriConnect helps you work smarter,
                sell better and achieve more.
              </p>
              <br />
              <Link to="/register" className="btn btn-primary">Get Started Free →</Link>
            </div>

            <img
              className="why-image"
              src="https://images.unsplash.com/photo-1464226184884-fa280b87c399?auto=format&fit=crop&w=900&q=85"
              alt="Agricultural farm landscape"
            />

            <div className="benefits">
              <div className="benefit">
                <i>👥</i>
                <div><h4>Empowering Farmers</h4><p>Give farmers the tools they need to manage, sell and grow.</p></div>
              </div>
              <div className="benefit">
                <i>🤝</i>
                <div><h4>Connecting Buyers</h4><p>Quality produce directly from trusted sources.</p></div>
              </div>
              <div className="benefit">
                <i>🌿</i>
                <div><h4>Growing Communities</h4><p>More income, healthier food and a better tomorrow.</p></div>
              </div>
            </div>
          </div>
        </section>

        <section className="cta" id="contact">
          <div className="container">
            <div className="cta-box">
              <h2>Ready to Grow Smarter?</h2>
              <p>Join AgriConnect and take your farm management and produce business to the next level.</p>
              <Link to="/register" className="btn">Create Your Account →</Link>
            </div>
          </div>
        </section>
      </main>

      <footer>
        <div className="container footer">
          <div className="logo">
            <div className="logo-icon">🌱</div>
            <div>
              <h2>AgriConnect</h2>
              <small>Farm Smarter. Grow Together.</small>
            </div>
          </div>
          <p>© 2026 AgriConnect Farm. All rights reserved.</p>
        </div>
      </footer>
    </div>
  );
}

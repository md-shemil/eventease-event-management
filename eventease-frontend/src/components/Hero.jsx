import React from 'react';

export default function Hero({ onExploreClick, onCreateClick }) {
  return (
    <section className="hero" id="home">
      <div className="hero-content">
        <p className="eyebrow">CURATED COMMUNITY EXPERIENCES</p>
        <h1>
          Every event starts<br />
          with a clear purpose.
        </h1>
        <p className="hero-description">
          Discover community gatherings, secure reserved seats with real-time capacity tracking,
          and manage event registrations directly in one place.
        </p>

        <div className="hero-actions">
          <a href="#events" className="btn btn-primary" onClick={onExploreClick}>
            Explore Events →
          </a>
          <button className="btn btn-secondary" onClick={onCreateClick}>
            + Create an Event
          </button>
        </div>
      </div>

      <div className="hero-art-badge" aria-hidden="true">
        <span className="hero-badge-title">ISSUE 2026</span>
        <span className="hero-badge-number">EE</span>
        <span className="hero-badge-sub">EST. CHENNAI</span>
      </div>
    </section>
  );
}

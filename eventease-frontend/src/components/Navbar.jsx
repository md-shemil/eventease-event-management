import React from 'react';
import { useAuth } from '../context/AuthContext';

export default function Navbar({
  onOpenAuth,
  onOpenCreate,
  onOpenMyRegistrations,
  onOpenAdminDashboard,
  searchTerm,
  onSearchChange,
}) {
  const { user, isAuthenticated, isAdmin, logout } = useAuth();

  return (
    <header className="navbar">
      <div className="navbar-brand">
        <a href="#home" className="logo">
          EventEase<span>.</span>
        </a>
      </div>

      <nav className="nav-links" aria-label="Main navigation">
        <a href="#home">Home</a>
        <a href="#events">Events</a>
        <a href="#about">About</a>
      </nav>

      <div className="navbar-search">
        <input
          type="text"
          placeholder="Search events, venues..."
          value={searchTerm}
          onChange={(e) => onSearchChange(e.target.value)}
          className="search-input"
        />
        {searchTerm && (
          <button
            type="button"
            className="search-clear-btn"
            onClick={() => onSearchChange('')}
            title="Clear search"
          >
            ×
          </button>
        )}
      </div>

      <div className="navbar-actions">
        {isAuthenticated ? (
          <div className="user-nav-group">
            <button
              className="btn btn-secondary btn-sm"
              onClick={onOpenMyRegistrations}
              title="View my registered events and bookmarks"
            >
              My Events
            </button>

            {isAdmin && (
              <button
                className="btn btn-admin btn-sm"
                onClick={onOpenAdminDashboard}
                title="Open Admin Dashboard"
              >
                Admin
              </button>
            )}

            <button
              className="btn btn-primary btn-sm"
              onClick={onOpenCreate}
            >
              + Create Event
            </button>

            <div className="user-profile-badge">
              <span className="user-name" title={user?.email}>
                {user?.name}
              </span>
              <span className={`role-tag role-${user?.role?.toLowerCase()}`}>
                {user?.role}
              </span>
            </div>

            <button
              className="btn btn-ghost btn-sm"
              onClick={logout}
              title="Sign out"
            >
              Sign Out
            </button>
          </div>
        ) : (
          <div className="guest-nav-group">
            <button
              className="btn btn-secondary btn-sm"
              onClick={() => onOpenAuth('login')}
            >
              Sign In
            </button>
            <button
              className="btn btn-primary btn-sm"
              onClick={() => onOpenAuth('register')}
            >
              Get Started
            </button>
          </div>
        )}
      </div>
    </header>
  );
}

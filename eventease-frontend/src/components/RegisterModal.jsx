import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { MapPin, Calendar, Users } from 'lucide-react';

export default function RegisterModal({
  event,
  isOpen,
  onClose,
  onSubmit,
  loading,
  error,
  onOpenAuth,
}) {
  const { user, isAuthenticated } = useAuth();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');

  useEffect(() => {
    if (user) {
      setName(user.name || '');
      setEmail(user.email || '');
    } else {
      setName('');
      setEmail('');
    }
  }, [user, isOpen]);

  if (!isOpen || !event) return null;

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit({
      name: name.trim(),
      email: email.trim(),
    });
  };

  return (
    <div className="modal open" role="dialog" aria-modal="true">
      <div className="modal-backdrop" onClick={onClose} />
      <div className="modal-box">
        <div className="modal-heading">
          <div>
            <h2>Register for Event</h2>
            <p className="muted">You are reserving a spot for <strong>{event.name}</strong></p>
          </div>
          <button className="close-btn" type="button" onClick={onClose} aria-label="Close">
            ×
          </button>
        </div>

        <div className="modal-event-summary">
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: 4 }}>
            <MapPin size={12} strokeWidth={1.5} className="meta-icon" />
            {event.venue}
          </span>
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: 4 }}>
            <Calendar size={12} strokeWidth={1.5} className="meta-icon" />
            {event.date}
          </span>
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: 4 }}>
            <Users size={12} strokeWidth={1.5} className="meta-icon" />
            {event.availableCapacity} seats remaining
          </span>
        </div>

        {!isAuthenticated && (
          <div className="guest-info-banner">
            <span>Already have an EventEase account?</span>
            <button
              type="button"
              className="btn-link"
              onClick={() => {
                onClose();
                onOpenAuth('login');
              }}
            >
              Sign In
            </button>
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="regName">Full Name</label>
            <input
              id="regName"
              type="text"
              required
              maxLength={150}
              placeholder="e.g. Alex Johnson"
              value={name}
              onChange={(e) => setName(e.target.value)}
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="regEmail">Email Address</label>
            <input
              id="regEmail"
              type="email"
              required
              maxLength={254}
              placeholder="alex@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              disabled={loading}
            />
          </div>

          {error && <div className="form-error visible">{error}</div>}

          <div className="form-actions">
            <button
              type="button"
              className="btn btn-secondary"
              onClick={onClose}
              disabled={loading}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={loading || event.availableCapacity <= 0}
            >
              {loading ? 'Confirming...' : 'Confirm Registration'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

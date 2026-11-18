import React, { useState, useEffect } from 'react';
import { eventService } from '../services/eventService';
import LoadingSpinner from './LoadingSpinner';

export default function RegistrationsModal({ event, isOpen, onClose }) {
  const [registrations, setRegistrations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');

  useEffect(() => {
    if (isOpen && event) {
      setLoading(true);
      setError('');
      eventService.getRegistrations(event.id)
        .then((data) => {
          setRegistrations(data || []);
        })
        .catch((err) => {
          setError(err.message || 'Failed to load registrations.');
        })
        .finally(() => {
          setLoading(false);
        });
    }
  }, [isOpen, event]);

  if (!isOpen || !event) return null;

  const filtered = registrations.filter((r) => {
    if (!search.trim()) return true;
    const q = search.toLowerCase();
    return (
      (r.userName && r.userName.toLowerCase().includes(q)) ||
      (r.userEmail && r.userEmail.toLowerCase().includes(q))
    );
  });

  const formatDate = (dateStr) => {
    if (!dateStr) return '';
    try {
      return new Date(dateStr).toLocaleString('en-IN', {
        day: 'numeric',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return dateStr;
    }
  };

  return (
    <div className="modal open" role="dialog" aria-modal="true">
      <div className="modal-backdrop" onClick={onClose} />
      <div className="modal-box modal-lg">
        <div className="modal-heading">
          <div>
            <h2>Event Registrations</h2>
            <p className="muted">
              Attendees for <strong>{event.name}</strong> ({registrations.length} / {event.capacity} registered)
            </p>
          </div>
          <button className="close-btn" type="button" onClick={onClose} aria-label="Close">
            ×
          </button>
        </div>

        <div className="registrations-toolbar">
          <input
            type="text"
            className="search-input"
            placeholder="Search attendee by name or email..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>

        {loading && <LoadingSpinner message="Fetching attendee list..." />}

        {error && <div className="form-error visible">{error}</div>}

        {!loading && !error && filtered.length === 0 && (
          <div className="empty-state">
            <h4>No attendees found</h4>
            <p className="text-muted">
              {registrations.length === 0
                ? 'No one has registered for this event yet.'
                : 'No attendees match your search.'}
            </p>
          </div>
        )}

        {!loading && !error && filtered.length > 0 && (
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Attendee Name</th>
                  <th>Email Address</th>
                  <th>Registered At</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((reg, index) => (
                  <tr key={reg.id || index}>
                    <td>{index + 1}</td>
                    <td className="font-medium">{reg.userName || 'Anonymous'}</td>
                    <td>{reg.userEmail}</td>
                    <td className="text-muted">{formatDate(reg.registrationDate)}</td>
                    <td>
                      <span className="badge badge-success">
                        {reg.status || 'CONFIRMED'}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <div className="modal-footer">
          <button type="button" className="btn btn-secondary" onClick={onClose}>
            Close
          </button>
        </div>
      </div>
    </div>
  );
}

import React from 'react';
import { useAuth } from '../context/AuthContext';
import { MapPin, Calendar, Users, Tag, Bookmark } from 'lucide-react';

export default function EventDetailsModal({
  event,
  onClose,
  onRegister,
  onCancelRegistration,
  onToggleBookmark,
}) {
  if (!event) return null;
  const { isAuthenticated } = useAuth();

  const isFull = event.availableCapacity <= 0 || event.status === 'FULL';
  const percentage = Math.min(100, Math.round(((event.registeredCount || 0) / (event.capacity || 1)) * 100));

  const formatDate = (dateString) => {
    if (!dateString) return 'Date not specified';
    try {
      const date = new Date(`${dateString}T00:00:00`);
      return date.toLocaleDateString('en-IN', {
        weekday: 'long',
        day: 'numeric',
        month: 'long',
        year: 'numeric',
      });
    } catch {
      return dateString;
    }
  };

  return (
    <div className="modal open" role="dialog" aria-modal="true">
      <div className="modal-backdrop" onClick={onClose} />
      <div className="modal-box modal-lg">
        <div className={`modal-banner-header ${event.bannerTheme || 'editorial'}`}>
          <div className="banner-header-content">
            <div>
              <span className="badge-light">{event.categoryName || 'Curated Gathering'}</span>
              <h2 className="modal-event-title">{event.name}</h2>
            </div>
          </div>
          <button className="close-btn" type="button" onClick={onClose} aria-label="Close">
            ×
          </button>
        </div>

        <div className="modal-body">
          <div className="event-info-grid">
            <div className="info-tile">
              <MapPin size={16} strokeWidth={1.5} className="meta-icon" />
              <div>
                <span className="info-label">Venue</span>
                <span className="info-value">{event.venue || 'To be announced'}</span>
              </div>
            </div>

            <div className="info-tile">
              <Calendar size={16} strokeWidth={1.5} className="meta-icon" />
              <div>
                <span className="info-label">Date & Time</span>
                <span className="info-value">
                  {formatDate(event.date)} <br />
                  <small>{event.time || '10:00 AM'}</small>
                </span>
              </div>
            </div>

            <div className="info-tile">
              <Users size={16} strokeWidth={1.5} className="meta-icon" />
              <div>
                <span className="info-label">Capacity Status</span>
                <span className="info-value">
                  {event.registeredCount || 0} / {event.capacity} seats ({isFull ? 'Sold Out' : `${event.availableCapacity} left`})
                </span>
              </div>
            </div>

            <div className="info-tile">
              <Tag size={16} strokeWidth={1.5} className="meta-icon" />
              <div>
                <span className="info-label">Organizer</span>
                <span className="info-value">{event.organizerName || 'EventEase Editorial'}</span>
              </div>
            </div>
          </div>

          <div className="capacity-section">
            <div className="capacity-label-row">
              <span className="text-muted">Registration Progress</span>
              <span className="font-semibold">{percentage}% Filled</span>
            </div>
            <div className="capacity-bar-track">
              <div
                className={`capacity-bar-fill ${isFull ? 'bar-full' : percentage > 80 ? 'bar-warning' : ''}`}
                style={{ width: `${percentage}%` }}
              />
            </div>
          </div>

          <div className="description-section">
            <h4 className="section-subtitle">About This Event</h4>
            <p className="modal-description-text">
              {event.description || 'No detailed description provided for this event yet.'}
            </p>
          </div>
        </div>

        <div className="modal-footer">
          <button
            type="button"
            className="btn btn-secondary"
            onClick={() => onToggleBookmark(event)}
          >
            <Bookmark size={13} strokeWidth={1.5} fill={event.bookmarked ? 'currentColor' : 'none'} style={{ marginRight: 6 }} />
            {event.bookmarked ? 'Saved' : 'Bookmark'}
          </button>

          <div className="modal-actions-right">
            <button type="button" className="btn btn-secondary" onClick={onClose}>
              Close
            </button>
            {event.registered ? (
              <button
                type="button"
                className="btn btn-danger"
                onClick={() => {
                  onCancelRegistration(event);
                  onClose();
                }}
              >
                Cancel Registration
              </button>
            ) : (
              <button
                type="button"
                className="btn btn-primary"
                disabled={isFull}
                onClick={() => {
                  onRegister(event);
                  onClose();
                }}
              >
                {isFull ? 'Sold Out' : 'Register Now'}
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

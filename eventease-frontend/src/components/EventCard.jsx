import React from 'react';
import { useAuth } from '../context/AuthContext';
import { MapPin, Calendar, Users, Bookmark } from 'lucide-react';

export default function EventCard({
  event,
  index,
  onRegister,
  onCancelRegistration,
  onViewDetails,
  onViewAttendees,
  onEdit,
  onDelete,
  onToggleBookmark,
}) {
  const { isAdmin } = useAuth();

  const bannerThemes = ['editorial', 'terracotta', 'forest', 'ochre', 'sand', 'rust'];
  const theme = event.bannerTheme || bannerThemes[index % bannerThemes.length];

  const isFull = event.availableCapacity <= 0 || event.status === 'FULL';
  const percentage = Math.min(100, Math.round(((event.registeredCount || 0) / (event.capacity || 1)) * 100));

  const formatDate = (dateString) => {
    if (!dateString) return 'Date not specified';
    try {
      const date = new Date(`${dateString}T00:00:00`);
      return date.toLocaleDateString('en-IN', {
        day: 'numeric',
        month: 'short',
        year: 'numeric',
      });
    } catch {
      return dateString;
    }
  };

  return (
    <article className="event-card">
      <div className={`event-banner ${theme}`}>
        <span className="banner-category">
          {event.categoryName || 'Curated Gathering'}
        </span>

        <button
          type="button"
          className={`bookmark-btn ${event.bookmarked ? 'bookmarked' : ''}`}
          onClick={(e) => {
            e.stopPropagation();
            onToggleBookmark(event);
          }}
          title={event.bookmarked ? 'Remove bookmark' : 'Bookmark event'}
          aria-label="Bookmark"
        >
          <Bookmark size={13} strokeWidth={1.5} fill={event.bookmarked ? 'currentColor' : 'none'} />
        </button>
      </div>

      <div className="event-content">
        <div className="event-header-row">
          <span className={`status-pill ${isFull ? 'status-full' : 'status-open'}`}>
            {isFull ? 'Sold Out' : 'Registration Open'}
          </span>
          {event.registered && (
            <span className="status-pill status-registered">
              Registered
            </span>
          )}
        </div>

        <h3 className="event-title" onClick={() => onViewDetails(event)}>
          {event.name}
        </h3>

        {event.description && (
          <p className="event-description-snippet">
            {event.description}
          </p>
        )}

        <div className="event-meta">
          <span className="meta-item">
            <MapPin size={12} strokeWidth={1.5} className="meta-icon" />
            <span>{event.venue || 'Venue TBA'}</span>
          </span>
          <span className="meta-item">
            <Calendar size={12} strokeWidth={1.5} className="meta-icon" />
            <span>{formatDate(event.date)} {event.time ? `· ${event.time}` : ''}</span>
          </span>
        </div>

        {/* Capacity Progress Bar */}
        <div className="capacity-container">
          <div className="capacity-label-row">
            <span className="capacity-text">
              <Users size={11} strokeWidth={1.5} style={{ display: 'inline', verticalAlign: '-1px', marginRight: 4 }} />
              {event.registeredCount || 0} / {event.capacity} Registered
            </span>
            <span className={`capacity-spots ${event.availableCapacity <= 5 ? 'spots-low' : ''}`}>
              {isFull ? 'No spots left' : `${event.availableCapacity} spots left`}
            </span>
          </div>
          <div className="capacity-bar-track">
            <div
              className={`capacity-bar-fill ${isFull ? 'bar-full' : percentage > 80 ? 'bar-warning' : ''}`}
              style={{ width: `${percentage}%` }}
            />
          </div>
        </div>

        {/* Action Buttons */}
        <div className="event-card-actions">
          {event.registered ? (
            <button
              className="btn btn-danger-subtle btn-block"
              onClick={() => onCancelRegistration(event)}
            >
              Cancel Registration
            </button>
          ) : (
            <button
              className={`btn btn-block ${isFull ? 'btn-disabled' : 'btn-primary'}`}
              disabled={isFull}
              onClick={() => onRegister(event)}
            >
              {isFull ? 'Event Full' : 'Register Now'}
            </button>
          )}

          <div className="event-secondary-actions">
            <button
              className="btn btn-secondary btn-sm"
              onClick={() => onViewDetails(event)}
            >
              Details
            </button>

            {isAdmin && (
              <>
                <button
                  className="btn btn-secondary btn-sm"
                  onClick={() => onViewAttendees(event)}
                  title="View registered attendees"
                >
                  Attendees ({event.registeredCount || 0})
                </button>
                <button
                  className="btn btn-ghost btn-sm"
                  onClick={() => onEdit(event)}
                  title="Edit event"
                >
                  Edit
                </button>
                <button
                  className="btn btn-ghost btn-sm text-danger"
                  onClick={() => onDelete(event)}
                  title="Delete event"
                >
                  Delete
                </button>
              </>
            )}
          </div>
        </div>
      </div>
    </article>
  );
}

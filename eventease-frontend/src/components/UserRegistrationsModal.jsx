import React, { useState, useEffect } from 'react';
import { userService } from '../services/userService';
import LoadingSpinner from './LoadingSpinner';
import { MapPin, Calendar, Mail, Bookmark, Ticket, Bell } from 'lucide-react';

export default function UserRegistrationsModal({
  isOpen,
  onClose,
  onCancelRegistration,
  onSelectEvent,
}) {
  const [activeTab, setActiveTab] = useState('registrations'); // 'registrations' | 'bookmarks' | 'notifications'
  const [registrations, setRegistrations] = useState([]);
  const [bookmarks, setBookmarks] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadData = () => {
    setLoading(true);
    setError('');
    Promise.all([
      userService.getMyRegistrations().catch(() => []),
      userService.getMyBookmarks().catch(() => []),
      userService.getNotifications().catch(() => []),
    ])
      .then(([regs, bks, notifs]) => {
        setRegistrations(regs || []);
        setBookmarks(bks || []);
        setNotifications(notifs || []);
      })
      .catch((err) => {
        setError(err.message || 'Failed to load user data');
      })
      .finally(() => {
        setLoading(false);
      });
  };

  useEffect(() => {
    if (isOpen) {
      loadData();
    }
  }, [isOpen]);

  if (!isOpen) return null;

  const handleCancel = async (eventId) => {
    if (window.confirm('Are you sure you want to cancel your registration for this event?')) {
      try {
        await onCancelRegistration({ id: eventId });
        loadData();
      } catch (err) {
        alert(err.message || 'Failed to cancel registration');
      }
    }
  };

  const handleMarkNotification = async (id) => {
    try {
      await userService.markNotificationRead(id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, read: true } : n))
      );
    } catch {
      // ignore
    }
  };

  return (
    <div className="modal open" role="dialog" aria-modal="true">
      <div className="modal-backdrop" onClick={onClose} />
      <div className="modal-box modal-lg">
        <div className="modal-heading">
          <div>
            <h2>My Activity</h2>
            <p className="muted">Manage your event bookings, saved bookmarks, and notifications.</p>
          </div>
          <button className="close-btn" type="button" onClick={onClose} aria-label="Close">
            ×
          </button>
        </div>

        <div className="tab-bar">
          <button
            type="button"
            className={`tab-btn ${activeTab === 'registrations' ? 'active' : ''}`}
            onClick={() => setActiveTab('registrations')}
          >
            My Bookings ({registrations.length})
          </button>
          <button
            type="button"
            className={`tab-btn ${activeTab === 'bookmarks' ? 'active' : ''}`}
            onClick={() => setActiveTab('bookmarks')}
          >
            Bookmarks ({bookmarks.length})
          </button>
          <button
            type="button"
            className={`tab-btn ${activeTab === 'notifications' ? 'active' : ''}`}
            onClick={() => setActiveTab('notifications')}
          >
            Notifications ({notifications.filter((n) => !n.read).length})
          </button>
        </div>

        {loading && <LoadingSpinner message="Loading your activity..." />}
        {error && <div className="form-error visible">{error}</div>}

        {!loading && !error && activeTab === 'registrations' && (
          <div className="tab-content">
            {registrations.length === 0 ? (
              <div className="empty-state">
                <h4>No registered events</h4>
                <p className="text-muted">You have not registered for any events yet. Explore upcoming gatherings to reserve a spot.</p>
              </div>
            ) : (
              <div className="user-event-list">
                {registrations.map((reg) => (
                  <div className="user-event-item" key={reg.id}>
                    <div className="user-event-info">
                      <h4>{reg.eventName}</h4>
                      <p className="text-muted">
                        <MapPin size={11} strokeWidth={1.5} className="meta-icon" style={{ verticalAlign: '-1px', marginRight: 4 }} />
                        {reg.eventVenue} · 
                        <Calendar size={11} strokeWidth={1.5} className="meta-icon" style={{ verticalAlign: '-1px', margin: '0 4px 0 8px' }} />
                        {reg.eventDate} {reg.eventTime ? `· ${reg.eventTime}` : ''}
                      </p>
                      <span className="badge badge-success">Status: {reg.status}</span>
                    </div>
                    <div className="user-event-actions">
                      <button
                        type="button"
                        className="btn btn-secondary btn-sm"
                        onClick={() => {
                          onClose();
                          onSelectEvent(reg.eventId);
                        }}
                      >
                        View Event
                      </button>
                      <button
                        type="button"
                        className="btn btn-danger-subtle btn-sm"
                        onClick={() => handleCancel(reg.eventId)}
                      >
                        Cancel Booking
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {!loading && !error && activeTab === 'bookmarks' && (
          <div className="tab-content">
            {bookmarks.length === 0 ? (
              <div className="empty-state">
                <h4>No bookmarked events</h4>
                <p className="text-muted">Click Bookmark on any event card to save it here for reference.</p>
              </div>
            ) : (
              <div className="user-event-list">
                {bookmarks.map((bm) => (
                  <div className="user-event-item" key={bm.id}>
                    <div className="user-event-info">
                      <h4>{bm.eventName}</h4>
                      <p className="text-muted">
                        <MapPin size={11} strokeWidth={1.5} className="meta-icon" style={{ verticalAlign: '-1px', marginRight: 4 }} />
                        {bm.eventVenue} · 
                        <Calendar size={11} strokeWidth={1.5} className="meta-icon" style={{ verticalAlign: '-1px', margin: '0 4px 0 8px' }} />
                        {bm.eventDate}
                      </p>
                    </div>
                    <div className="user-event-actions">
                      <button
                        type="button"
                        className="btn btn-primary btn-sm"
                        onClick={() => {
                          onClose();
                          onSelectEvent(bm.eventId);
                        }}
                      >
                        View & Register
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {!loading && !error && activeTab === 'notifications' && (
          <div className="tab-content">
            {notifications.length === 0 ? (
              <div className="empty-state">
                <h4>No notifications</h4>
                <p className="text-muted">You are completely up to date.</p>
              </div>
            ) : (
              <div className="notification-list">
                {notifications.map((n) => (
                  <div
                    key={n.id}
                    className={`notification-item ${n.read ? 'read' : 'unread'}`}
                    onClick={() => !n.read && handleMarkNotification(n.id)}
                  >
                    <div className="notification-icon" style={{ marginTop: 2, color: 'var(--primary)' }}>
                      <Mail size={14} strokeWidth={1.5} />
                    </div>
                    <div className="notification-content">
                      <div className="notification-title">{n.title}</div>
                      <div className="notification-msg">{n.message}</div>
                      <small className="notification-time">
                        {new Date(n.createdAt).toLocaleTimeString([], {
                          hour: '2-digit',
                          minute: '2-digit',
                        })}
                      </small>
                    </div>
                    {!n.read && <span className="unread-dot" />}
                  </div>
                ))}
              </div>
            )}
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

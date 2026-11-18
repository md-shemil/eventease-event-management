import React, { useState, useEffect } from 'react';
import { adminService } from '../services/adminService';
import LoadingSpinner from './LoadingSpinner';
import { Calendar, Ticket, Users, TrendingUp } from 'lucide-react';

export default function AdminDashboardModal({
  isOpen,
  onClose,
  onEditEvent,
  onDeleteEvent,
  onViewAttendees,
  onCreateEvent,
}) {
  const [activeTab, setActiveTab] = useState('overview'); // 'overview' | 'events' | 'users' | 'registrations'
  const [dashboard, setDashboard] = useState(null);
  const [events, setEvents] = useState([]);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadAdminData = () => {
    setLoading(true);
    setError('');
    Promise.all([
      adminService.getDashboard(),
      adminService.getAdminEvents(),
      adminService.getAdminUsers(),
    ])
      .then(([dashData, eventsData, usersData]) => {
        setDashboard(dashData);
        setEvents(eventsData || []);
        setUsers(usersData || []);
      })
      .catch((err) => {
        setError(err.message || 'Failed to load administrative dashboard.');
      })
      .finally(() => {
        setLoading(false);
      });
  };

  useEffect(() => {
    if (isOpen) {
      loadAdminData();
    }
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div className="modal open" role="dialog" aria-modal="true">
      <div className="modal-backdrop" onClick={onClose} />
      <div className="modal-box modal-xl">
        <div className="modal-heading">
          <div>
            <h2>Admin Control Center</h2>
            <p className="muted">Platform metrics, user directory, and capacity management.</p>
          </div>
          <button className="close-btn" type="button" onClick={onClose} aria-label="Close">
            ×
          </button>
        </div>

        <div className="tab-bar">
          <button
            type="button"
            className={`tab-btn ${activeTab === 'overview' ? 'active' : ''}`}
            onClick={() => setActiveTab('overview')}
          >
            Overview
          </button>
          <button
            type="button"
            className={`tab-btn ${activeTab === 'events' ? 'active' : ''}`}
            onClick={() => setActiveTab('events')}
          >
            Manage Events ({events.length})
          </button>
          <button
            type="button"
            className={`tab-btn ${activeTab === 'users' ? 'active' : ''}`}
            onClick={() => setActiveTab('users')}
          >
            Users ({users.length})
          </button>
          <button
            type="button"
            className={`tab-btn ${activeTab === 'registrations' ? 'active' : ''}`}
            onClick={() => setActiveTab('registrations')}
          >
            Recent Registrations
          </button>
        </div>

        {loading && <LoadingSpinner message="Fetching administrative metrics..." />}
        {error && <div className="form-error visible">{error}</div>}

        {!loading && !error && activeTab === 'overview' && dashboard && (
          <div className="admin-overview">
            {/* Stat Cards Grid */}
            <div className="stats-grid">
              <div className="stat-card">
                <Calendar size={18} strokeWidth={1.5} className="meta-icon" />
                <div className="stat-val">{dashboard.totalEvents}</div>
                <div className="stat-label">Total Events</div>
              </div>

              <div className="stat-card">
                <Ticket size={18} strokeWidth={1.5} className="meta-icon" />
                <div className="stat-val">{dashboard.totalRegistrations}</div>
                <div className="stat-label">Total Bookings</div>
              </div>

              <div className="stat-card">
                <Users size={18} strokeWidth={1.5} className="meta-icon" />
                <div className="stat-val">{dashboard.totalUsers}</div>
                <div className="stat-label">Registered Users</div>
              </div>

              <div className="stat-card">
                <TrendingUp size={18} strokeWidth={1.5} className="meta-icon" />
                <div className="stat-val">{dashboard.upcomingEventsCount}</div>
                <div className="stat-label">Upcoming Events</div>
              </div>
            </div>

            {/* Category Distribution */}
            <div className="admin-section">
              <h3 className="section-subtitle" style={{ fontSize: 13, marginBottom: 12 }}>
                Events by Category
              </h3>
              <div className="category-stats-grid">
                {dashboard.categoryDistribution?.map((cat) => (
                  <div key={cat.id} className="cat-stat-card">
                    <div className="cat-stat-name">{cat.name}</div>
                    <div className="cat-stat-count">{cat.eventCount} events</div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {!loading && !error && activeTab === 'events' && (
          <div className="admin-tab-table">
            <div className="table-header-actions">
              <h4>All Events ({events.length})</h4>
              <button
                type="button"
                className="btn btn-primary btn-sm"
                onClick={() => {
                  onClose();
                  onCreateEvent();
                }}
              >
                + New Event
              </button>
            </div>

            <div className="table-responsive">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Event Name</th>
                    <th>Category</th>
                    <th>Date</th>
                    <th>Venue</th>
                    <th>Capacity</th>
                    <th>Bookings</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {events.map((evt) => (
                    <tr key={evt.id}>
                      <td>#{evt.id}</td>
                      <td className="font-semibold">{evt.name}</td>
                      <td>
                        <span className="badge badge-subtle">{evt.categoryName || 'General'}</span>
                      </td>
                      <td>{evt.date}</td>
                      <td>{evt.venue}</td>
                      <td>{evt.capacity}</td>
                      <td>
                        <span className={`badge ${evt.availableCapacity <= 0 ? 'badge-danger' : 'badge-success'}`}>
                          {evt.registeredCount || 0} / {evt.capacity}
                        </span>
                      </td>
                      <td>
                        <div className="action-buttons-inline">
                          <button
                            type="button"
                            className="btn btn-secondary btn-xs"
                            onClick={() => {
                              onClose();
                              onViewAttendees(evt);
                            }}
                          >
                            Attendees
                          </button>
                          <button
                            type="button"
                            className="btn btn-ghost btn-xs"
                            onClick={() => {
                              onClose();
                              onEditEvent(evt);
                            }}
                          >
                            Edit
                          </button>
                          <button
                            type="button"
                            className="btn btn-ghost btn-xs text-danger"
                            onClick={() => {
                              if (window.confirm(`Delete event "${evt.name}"?`)) {
                                onDeleteEvent(evt);
                                loadAdminData();
                              }
                            }}
                          >
                            Delete
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {!loading && !error && activeTab === 'users' && (
          <div className="admin-tab-table">
            <h4>Registered Platform Users ({users.length})</h4>
            <div className="table-responsive" style={{ marginTop: 12 }}>
              <table className="data-table">
                <thead>
                  <tr>
                    <th>User ID</th>
                    <th>Full Name</th>
                    <th>Email Address</th>
                    <th>Role</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map((u) => (
                    <tr key={u.id}>
                      <td>#{u.id}</td>
                      <td className="font-medium">{u.name}</td>
                      <td>{u.email}</td>
                      <td>
                        <span className={`badge ${u.role === 'ADMIN' ? 'badge-admin' : 'badge-user'}`}>
                          {u.role}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {!loading && !error && activeTab === 'registrations' && dashboard && (
          <div className="admin-tab-table">
            <h4>Recent Registrations ({dashboard.recentRegistrations?.length || 0})</h4>
            <div className="table-responsive" style={{ marginTop: 12 }}>
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Reg ID</th>
                    <th>Event</th>
                    <th>Participant</th>
                    <th>Email</th>
                    <th>Date Registered</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {dashboard.recentRegistrations?.map((r) => (
                    <tr key={r.id}>
                      <td>#{r.id}</td>
                      <td className="font-medium">{r.eventName}</td>
                      <td>{r.userName}</td>
                      <td>{r.userEmail}</td>
                      <td className="text-muted">
                        {r.registrationDate ? new Date(r.registrationDate).toLocaleDateString() : ''}
                      </td>
                      <td>
                        <span className="badge badge-success">{r.status}</span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        <div className="modal-footer">
          <button type="button" className="btn btn-secondary" onClick={onClose}>
            Close Dashboard
          </button>
        </div>
      </div>
    </div>
  );
}

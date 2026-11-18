import React, { useState, useEffect, useCallback } from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import { eventService } from './services/eventService';
import Navbar from './components/Navbar';
import Hero from './components/Hero';
import CategoryFilter from './components/CategoryFilter';
import EventCard from './components/EventCard';
import EventDetailsModal from './components/EventDetailsModal';
import RegisterModal from './components/RegisterModal';
import CreateEventModal from './components/CreateEventModal';
import RegistrationsModal from './components/RegistrationsModal';
import UserRegistrationsModal from './components/UserRegistrationsModal';
import AdminDashboardModal from './components/AdminDashboardModal';
import AuthModal from './components/AuthModal';
import Toast from './components/Toast';
import LoadingSpinner from './components/LoadingSpinner';
import { AlertCircle } from 'lucide-react';
import './App.css';

function MainApp() {
  const { user, isAuthenticated, isAdmin } = useAuth();

  // Data states
  const [events, setEvents] = useState([]);
  const [categories, setCategories] = useState([]);
  const [selectedCategoryId, setSelectedCategoryId] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Toast notification state
  const [toast, setToast] = useState(null);

  // Modal states
  const [authModal, setAuthModal] = useState({ isOpen: false, mode: 'login' });
  const [createModal, setCreateModal] = useState({ isOpen: false, editEvent: null });
  const [detailsModal, setDetailsModal] = useState({ isOpen: false, event: null });
  const [registerModal, setRegisterModal] = useState({ isOpen: false, event: null });
  const [registrationsModal, setRegistrationsModal] = useState({ isOpen: false, event: null });
  const [userRegistrationsModal, setUserRegistrationsModal] = useState({ isOpen: false });
  const [adminDashboardModal, setAdminDashboardModal] = useState({ isOpen: false });

  const [registerLoading, setRegisterLoading] = useState(false);
  const [registerError, setRegisterError] = useState('');
  const [createLoading, setCreateLoading] = useState(false);
  const [createError, setCreateError] = useState('');

  const showToast = (message, type = 'success') => {
    setToast({ message, type });
    setTimeout(() => {
      setToast(null);
    }, 4500);
  };

  const loadCategories = useCallback(async () => {
    try {
      const data = await eventService.getCategories();
      setCategories(data || []);
    } catch (err) {
      console.warn('Could not load categories:', err);
    }
  }, []);

  const loadEvents = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await eventService.getAll(selectedCategoryId, searchTerm);
      setEvents(data || []);
    } catch (err) {
      console.error('Failed to load events:', err);
      setError(err.message || 'Could not load events. Make sure Spring Boot is running on localhost:8080.');
    } finally {
      setLoading(false);
    }
  }, [selectedCategoryId, searchTerm]);

  useEffect(() => {
    loadCategories();
  }, [loadCategories]);

  useEffect(() => {
    const timer = setTimeout(() => {
      loadEvents();
    }, 250);
    return () => clearTimeout(timer);
  }, [loadEvents]);

  // Handle Event Registration
  const handleOpenRegister = (event) => {
    setRegisterError('');
    setRegisterModal({ isOpen: true, event });
  };

  const handleConfirmRegister = async (participantData) => {
    if (!registerModal.event) return;
    setRegisterLoading(true);
    setRegisterError('');

    try {
      if (isAuthenticated) {
        await eventService.register(registerModal.event.id, participantData);
      } else {
        await eventService.registerLegacy(registerModal.event.id, participantData);
      }

      showToast(`Successfully registered for "${registerModal.event.name}"!`);
      setRegisterModal({ isOpen: false, event: null });
      loadEvents();
      loadCategories();
    } catch (err) {
      setRegisterError(err.message || 'Registration failed. Please try again.');
    } finally {
      setRegisterLoading(false);
    }
  };

  // Handle Cancel Registration
  const handleCancelRegistration = async (event) => {
    try {
      await eventService.cancelRegistration(event.id);
      showToast(`Cancelled registration for "${event.name}".`);
      loadEvents();
      loadCategories();
    } catch (err) {
      showToast(err.message || 'Failed to cancel registration.', 'error');
    }
  };

  // Handle Bookmark Toggle
  const handleToggleBookmark = async (event) => {
    if (!isAuthenticated) {
      setAuthModal({ isOpen: true, mode: 'login' });
      showToast('Please sign in to bookmark events.', 'info');
      return;
    }

    try {
      if (event.bookmarked) {
        await eventService.removeBookmark(event.id);
        showToast(`Removed "${event.name}" from bookmarks.`);
      } else {
        await eventService.bookmark(event.id);
        showToast(`Bookmarked "${event.name}"!`);
      }
      loadEvents();
    } catch (err) {
      showToast(err.message || 'Failed to update bookmark.', 'error');
    }
  };

  // Handle Create or Update Event
  const handleCreateOrUpdate = async (payload) => {
    setCreateLoading(true);
    setCreateError('');

    try {
      if (createModal.editEvent) {
        await eventService.update(createModal.editEvent.id, payload);
        showToast(`Event "${payload.name}" updated successfully!`);
      } else {
        await eventService.create(payload);
        showToast(`Event "${payload.name}" published successfully!`);
      }
      setCreateModal({ isOpen: false, editEvent: null });
      loadEvents();
      loadCategories();
    } catch (err) {
      setCreateError(err.message || 'Failed to save event. Check inputs.');
    } finally {
      setCreateLoading(false);
    }
  };

  // Handle Delete Event
  const handleDeleteEvent = async (event) => {
    if (!window.confirm(`Are you sure you want to delete event "${event.name}"? This cannot be undone.`)) {
      return;
    }

    try {
      await eventService.delete(event.id);
      showToast(`Event "${event.name}" has been deleted.`);
      loadEvents();
      loadCategories();
    } catch (err) {
      showToast(err.message || 'Failed to delete event.', 'error');
    }
  };

  // Select Event from Bookmarks or Profile
  const handleSelectEventById = async (eventId) => {
    try {
      const event = await eventService.getById(eventId);
      if (event) {
        setDetailsModal({ isOpen: true, event });
      }
    } catch {
      showToast('Could not load event details.', 'error');
    }
  };

  return (
    <div className="app">
      {/* Toast Notification Container */}
      {toast && (
        <Toast
          message={toast.message}
          type={toast.type}
          onClose={() => setToast(null)}
        />
      )}

      {/* Main Navbar */}
      <Navbar
        onOpenAuth={(mode) => setAuthModal({ isOpen: true, mode })}
        onOpenCreate={() => {
          if (!isAuthenticated) {
            setAuthModal({ isOpen: true, mode: 'login' });
            showToast('Please sign in to create an event.', 'info');
          } else {
            setCreateError('');
            setCreateModal({ isOpen: true, editEvent: null });
          }
        }}
        onOpenMyRegistrations={() => setUserRegistrationsModal({ isOpen: true })}
        onOpenAdminDashboard={() => setAdminDashboardModal({ isOpen: true })}
        searchTerm={searchTerm}
        onSearchChange={setSearchTerm}
      />

      <main>
        {/* Hero Section */}
        <Hero
          onExploreClick={() => {
            const el = document.getElementById('events');
            if (el) el.scrollIntoView({ behavior: 'smooth' });
          }}
          onCreateClick={() => {
            if (!isAuthenticated) {
              setAuthModal({ isOpen: true, mode: 'login' });
              showToast('Please sign in to create an event.', 'info');
            } else {
              setCreateError('');
              setCreateModal({ isOpen: true, editEvent: null });
            }
          }}
        />

        {/* Events Discovery Section */}
        <section className="section" id="events">
          <div className="section-heading">
            <div>
              <p className="eyebrow">CURATED GATHERINGS</p>
              <h2>Upcoming Events</h2>
            </div>

            <div className="section-actions-right">
              <span className="event-count-badge">
                {events.length} {events.length === 1 ? 'event' : 'events'}
              </span>
              <button
                className="btn btn-secondary btn-sm"
                onClick={loadEvents}
                title="Refresh events list"
              >
                Refresh
              </button>
            </div>
          </div>

          {/* Category Pills Filter */}
          <CategoryFilter
            categories={categories}
            selectedCategoryId={selectedCategoryId}
            onSelectCategory={setSelectedCategoryId}
            totalEventsCount={events.length}
          />

          {/* Loading State */}
          {loading && <LoadingSpinner message="Fetching upcoming events..." />}

          {/* Error State */}
          {!loading && error && (
            <div className="error-card">
              <AlertCircle size={16} strokeWidth={1.5} className="error-icon" />
              <p className="error-text">{error}</p>
              <button className="btn btn-primary btn-sm" onClick={loadEvents}>
                Retry Connection
              </button>
            </div>
          )}

          {/* Empty State */}
          {!loading && !error && events.length === 0 && (
            <div className="empty-state-card">
              <h3>No events found</h3>
              <p className="muted">
                {searchTerm || selectedCategoryId
                  ? 'No events match your current filter or search criteria. Try resetting filters.'
                  : 'There are no upcoming events at the moment. Create the first event to get started.'}
              </p>
              <div className="empty-state-actions">
                {(searchTerm || selectedCategoryId) && (
                  <button
                    className="btn btn-secondary"
                    onClick={() => {
                      setSelectedCategoryId(null);
                      setSearchTerm('');
                    }}
                  >
                    Clear Filters
                  </button>
                )}
                <button
                  className="btn btn-primary"
                  onClick={() => {
                    if (!isAuthenticated) {
                      setAuthModal({ isOpen: true, mode: 'login' });
                    } else {
                      setCreateError('');
                      setCreateModal({ isOpen: true, editEvent: null });
                    }
                  }}
                >
                  + Create Event
                </button>
              </div>
            </div>
          )}

          {/* Events Grid */}
          {!loading && !error && events.length > 0 && (
            <div className="event-grid">
              {events.map((event, index) => (
                <EventCard
                  key={event.id}
                  event={event}
                  index={index}
                  onRegister={handleOpenRegister}
                  onCancelRegistration={handleCancelRegistration}
                  onViewDetails={(evt) => setDetailsModal({ isOpen: true, event: evt })}
                  onViewAttendees={(evt) => setRegistrationsModal({ isOpen: true, event: evt })}
                  onEdit={(evt) => {
                    setCreateError('');
                    setCreateModal({ isOpen: true, editEvent: evt });
                  }}
                  onDelete={handleDeleteEvent}
                  onToggleBookmark={handleToggleBookmark}
                />
              ))}
            </div>
          )}
        </section>

        {/* About Section */}
        <section className="section" id="about">
          <div className="message-card">
            <p className="eyebrow">ABOUT EVENTEASE</p>
            <h2>One place for every event experience.</h2>
            <p className="muted">
              Built with a high-performance Spring Boot backend and responsive React frontend.
              Experience thread-safe capacity validation, role-based authorization, and instant attendee bookings.
            </p>
          </div>
        </section>
      </main>

      <footer className="footer">
        <div className="footer-content">
          <p className="logo-sm">EventEase<span>.</span></p>
          <p className="footer-copyright">
            © 2026 EventEase · Curated gatherings and direct event reservation.
          </p>
        </div>
      </footer>

      {/* Modals */}
      <AuthModal
        isOpen={authModal.isOpen}
        initialMode={authModal.mode}
        onClose={() => setAuthModal({ isOpen: false, mode: 'login' })}
        onSuccess={(msg) => showToast(msg)}
      />

      <CreateEventModal
        isOpen={createModal.isOpen}
        editEvent={createModal.editEvent}
        categories={categories}
        onClose={() => setCreateModal({ isOpen: false, editEvent: null })}
        onSubmit={handleCreateOrUpdate}
        loading={createLoading}
        error={createError}
      />

      <EventDetailsModal
        event={detailsModal.event}
        onClose={() => setDetailsModal({ isOpen: false, event: null })}
        onRegister={handleOpenRegister}
        onCancelRegistration={handleCancelRegistration}
        onToggleBookmark={handleToggleBookmark}
      />

      <RegisterModal
        isOpen={registerModal.isOpen}
        event={registerModal.event}
        onClose={() => setRegisterModal({ isOpen: false, event: null })}
        onSubmit={handleConfirmRegister}
        loading={registerLoading}
        error={registerError}
        onOpenAuth={(mode) => setAuthModal({ isOpen: true, mode })}
      />

      <RegistrationsModal
        isOpen={registrationsModal.isOpen}
        event={registrationsModal.event}
        onClose={() => setRegistrationsModal({ isOpen: false, event: null })}
      />

      <UserRegistrationsModal
        isOpen={userRegistrationsModal.isOpen}
        onClose={() => setUserRegistrationsModal({ isOpen: false })}
        onCancelRegistration={handleCancelRegistration}
        onSelectEvent={handleSelectEventById}
      />

      <AdminDashboardModal
        isOpen={adminDashboardModal.isOpen}
        onClose={() => setAdminDashboardModal({ isOpen: false })}
        onEditEvent={(evt) => {
          setCreateError('');
          setCreateModal({ isOpen: true, editEvent: evt });
        }}
        onDeleteEvent={handleDeleteEvent}
        onViewAttendees={(evt) => setRegistrationsModal({ isOpen: true, event: evt })}
        onCreateEvent={() => {
          setCreateError('');
          setCreateModal({ isOpen: true, editEvent: null });
        }}
      />
    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <MainApp />
    </AuthProvider>
  );
}

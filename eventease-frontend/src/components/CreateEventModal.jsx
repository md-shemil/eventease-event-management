import React, { useState, useEffect } from 'react';

export default function CreateEventModal({
  isOpen,
  onClose,
  onSubmit,
  categories = [],
  editEvent = null,
  loading,
  error,
}) {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [venue, setVenue] = useState('');
  const [date, setDate] = useState('');
  const [time, setTime] = useState('10:00 AM');
  const [capacity, setCapacity] = useState('100');
  const [categoryId, setCategoryId] = useState('');
  const [bannerTheme, setBannerTheme] = useState('editorial');
  const [bannerIcon, setBannerIcon] = useState('');

  const themes = [
    { id: 'editorial', label: 'Editorial' },
    { id: 'terracotta', label: 'Terracotta' },
    { id: 'forest', label: 'Forest' },
    { id: 'ochre', label: 'Ochre' },
    { id: 'sand', label: 'Sand' },
    { id: 'rust', label: 'Rust' },
  ];

  useEffect(() => {
    if (editEvent) {
      setName(editEvent.name || '');
      setDescription(editEvent.description || '');
      setVenue(editEvent.venue || '');
      setDate(editEvent.date || '');
      setTime(editEvent.time || '10:00 AM');
      setCapacity(editEvent.capacity ? String(editEvent.capacity) : '100');
      setCategoryId(editEvent.categoryId ? String(editEvent.categoryId) : '');
      setBannerTheme(editEvent.bannerTheme || 'editorial');
      setBannerIcon(editEvent.bannerIcon || '');
    } else {
      setName('');
      setDescription('');
      setVenue('');
      setDate('');
      setTime('10:00 AM');
      setCapacity('100');
      setCategoryId(categories.length > 0 ? String(categories[0].id) : '');
      setBannerTheme('editorial');
      setBannerIcon('');
    }
  }, [editEvent, isOpen, categories]);

  if (!isOpen) return null;

  const handleSubmit = (e) => {
    e.preventDefault();
    const payload = {
      name: name.trim(),
      description: description.trim(),
      venue: venue.trim(),
      date,
      time,
      capacity: parseInt(capacity, 10),
      categoryId: categoryId ? parseInt(categoryId, 10) : null,
      bannerTheme,
      bannerIcon,
    };
    onSubmit(payload);
  };

  return (
    <div className="modal open" role="dialog" aria-modal="true">
      <div className="modal-backdrop" onClick={onClose} />
      <div className="modal-box modal-lg">
        <div className="modal-heading">
          <div>
            <h2>{editEvent ? 'Edit Event' : 'Create an Event'}</h2>
            <p className="muted">
              {editEvent
                ? 'Update event details and attendee capacity.'
                : 'Publish a new gathering to the EventEase directory.'}
            </p>
          </div>
          <button className="close-btn" type="button" onClick={onClose} aria-label="Close">
            ×
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="evtName">Event Name *</label>
            <input
              id="evtName"
              type="text"
              required
              maxLength={150}
              placeholder="e.g. Architecture & Design Symposium"
              value={name}
              onChange={(e) => setName(e.target.value)}
              disabled={loading}
            />
          </div>

          <div className="form-row-2">
            <div className="form-group">
              <label htmlFor="evtCategory">Category</label>
              <select
                id="evtCategory"
                value={categoryId}
                onChange={(e) => setCategoryId(e.target.value)}
                disabled={loading}
              >
                <option value="">Select a Category</option>
                {categories.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="evtVenue">Venue / Location *</label>
              <input
                id="evtVenue"
                type="text"
                required
                maxLength={200}
                placeholder="e.g. SRM University Campus"
                value={venue}
                onChange={(e) => setVenue(e.target.value)}
                disabled={loading}
              />
            </div>
          </div>

          <div className="form-row-3">
            <div className="form-group">
              <label htmlFor="evtDate">Date *</label>
              <input
                id="evtDate"
                type="date"
                required
                value={date}
                onChange={(e) => setDate(e.target.value)}
                disabled={loading}
              />
            </div>

            <div className="form-group">
              <label htmlFor="evtTime">Time</label>
              <input
                id="evtTime"
                type="text"
                placeholder="e.g. 10:00 AM"
                value={time}
                onChange={(e) => setTime(e.target.value)}
                disabled={loading}
              />
            </div>

            <div className="form-group">
              <label htmlFor="evtCapacity">Maximum Capacity *</label>
              <input
                id="evtCapacity"
                type="number"
                min="1"
                max="1000000"
                required
                placeholder="100"
                value={capacity}
                onChange={(e) => setCapacity(e.target.value)}
                disabled={loading}
              />
            </div>
          </div>

          <div className="form-group">
            <label>Card Banner Tone</label>
            <div className="theme-selector">
              {themes.map((t) => (
                <button
                  key={t.id}
                  type="button"
                  className={`theme-chip banner-${t.id} ${bannerTheme === t.id ? 'selected' : ''}`}
                  onClick={() => {
                    setBannerTheme(t.id);
                    setBannerIcon('');
                  }}
                >
                  <span>{t.label}</span>
                </button>
              ))}
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="evtDesc">Description</label>
            <textarea
              id="evtDesc"
              rows={3}
              placeholder="Provide a clear description of the program, expectations, and schedule."
              value={description}
              onChange={(e) => setDescription(e.target.value)}
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
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Saving...' : editEvent ? 'Update Event' : 'Save Event'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

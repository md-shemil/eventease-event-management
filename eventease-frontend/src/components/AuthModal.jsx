import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';

export default function AuthModal({
  isOpen,
  initialMode = 'login',
  onClose,
  onSuccess,
}) {
  const { login, register } = useAuth();
  const [mode, setMode] = useState(initialMode); // 'login' | 'register'
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('USER');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (mode === 'login') {
        await login(email.trim(), password);
      } else {
        await register(name.trim(), email.trim(), password, role);
      }
      onSuccess?.(mode === 'login' ? 'Welcome back!' : 'Account registered successfully!');
      onClose();
    } catch (err) {
      setError(err.message || 'Authentication failed. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  const fillCredentials = (type) => {
    if (type === 'admin') {
      setEmail('admin@eventease.com');
      setPassword('admin123');
    } else {
      setEmail('john@example.com');
      setPassword('password123');
    }
    setMode('login');
    setError('');
  };

  return (
    <div className="modal open" role="dialog" aria-modal="true">
      <div className="modal-backdrop" onClick={onClose} />
      <div className="modal-box modal-sm">
        <div className="modal-heading">
          <div>
            <h2>{mode === 'login' ? 'Sign In to EventEase' : 'Create an Account'}</h2>
            <p className="muted">
              {mode === 'login'
                ? 'Access your events, registrations, and dashboard.'
                : 'Join EventEase to discover and book experiences.'}
            </p>
          </div>
          <button className="close-btn" type="button" onClick={onClose} aria-label="Close">
            ×
          </button>
        </div>

        {/* Demo Credentials Helper */}
        <div className="demo-credentials-box">
          <span className="demo-label">Demo Access:</span>
          <div className="demo-buttons">
            <button
              type="button"
              className="btn btn-xs btn-outline"
              onClick={() => fillCredentials('admin')}
            >
              Admin Demo
            </button>
            <button
              type="button"
              className="btn btn-xs btn-outline"
              onClick={() => fillCredentials('user')}
            >
              User Demo
            </button>
          </div>
        </div>

        <div className="auth-tab-switch">
          <button
            type="button"
            className={`auth-tab-btn ${mode === 'login' ? 'active' : ''}`}
            onClick={() => {
              setMode('login');
              setError('');
            }}
          >
            Sign In
          </button>
          <button
            type="button"
            className={`auth-tab-btn ${mode === 'register' ? 'active' : ''}`}
            onClick={() => {
              setMode('register');
              setError('');
            }}
          >
            Create Account
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          {mode === 'register' && (
            <div className="form-group">
              <label htmlFor="authName">Full Name *</label>
              <input
                id="authName"
                type="text"
                required
                maxLength={100}
                placeholder="John Doe"
                value={name}
                onChange={(e) => setName(e.target.value)}
                disabled={loading}
              />
            </div>
          )}

          <div className="form-group">
            <label htmlFor="authEmail">Email Address *</label>
            <input
              id="authEmail"
              type="email"
              required
              maxLength={254}
              placeholder="you@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="authPassword">Password *</label>
            <input
              id="authPassword"
              type="password"
              required
              minLength={6}
              placeholder="At least 6 characters"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              disabled={loading}
            />
          </div>

          {mode === 'register' && (
            <div className="form-group">
              <label htmlFor="authRole">Account Role</label>
              <select
                id="authRole"
                value={role}
                onChange={(e) => setRole(e.target.value)}
                disabled={loading}
              >
                <option value="USER">Regular User</option>
                <option value="ADMIN">Administrator</option>
              </select>
            </div>
          )}

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
              {loading
                ? 'Processing...'
                : mode === 'login'
                ? 'Sign In'
                : 'Create Account'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

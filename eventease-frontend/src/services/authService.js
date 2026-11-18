import { api } from './api';

export const authService = {
  login: async (email, password) => {
    const data = await api.post('/auth/login', { email, password });
    if (data.token) {
      localStorage.setItem('eventease_token', data.token);
      localStorage.setItem('eventease_user', JSON.stringify(data.user));
    }
    return data;
  },

  register: async (name, email, password, role = 'USER') => {
    const data = await api.post('/auth/register', { name, email, password, role });
    if (data.token) {
      localStorage.setItem('eventease_token', data.token);
      localStorage.setItem('eventease_user', JSON.stringify(data.user));
    }
    return data;
  },

  logout: () => {
    localStorage.removeItem('eventease_token');
    localStorage.removeItem('eventease_user');
  },

  getCurrentUser: () => {
    const userStr = localStorage.getItem('eventease_user');
    try {
      return userStr ? JSON.parse(userStr) : null;
    } catch {
      return null;
    }
  },

  getToken: () => {
    return localStorage.getItem('eventease_token');
  },
};

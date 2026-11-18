import { api } from './api';

export const eventService = {
  getAll: (categoryId, search) => {
    const params = new URLSearchParams();
    if (categoryId) params.append('categoryId', categoryId);
    if (search && search.trim()) params.append('search', search.trim());
    const query = params.toString() ? `?${params.toString()}` : '';
    return api.get(`/events${query}`);
  },

  getById: (id) => {
    return api.get(`/events/${id}`);
  },

  create: (eventData) => {
    return api.post('/events', eventData);
  },

  update: (id, eventData) => {
    return api.put(`/events/${id}`, eventData);
  },

  delete: (id) => {
    return api.delete(`/events/${id}`);
  },

  register: (id, participantData) => {
    return api.post(`/events/${id}/register`, participantData || {});
  },

  registerLegacy: (id, participantData) => {
    return api.post(`/events/${id}/registrations`, participantData || {});
  },

  cancelRegistration: (id) => {
    return api.delete(`/events/${id}/register`);
  },

  bookmark: (id) => {
    return api.post(`/events/${id}/bookmark`, {});
  },

  removeBookmark: (id) => {
    return api.delete(`/events/${id}/bookmark`);
  },

  getRegistrations: (id) => {
    return api.get(`/events/${id}/registrations`);
  },

  getCategories: () => {
    return api.get('/categories');
  },
};

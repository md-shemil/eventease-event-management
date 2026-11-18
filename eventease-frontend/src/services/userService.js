import { api } from './api';

export const userService = {
  getProfile: () => {
    return api.get('/users/me');
  },

  getMyRegistrations: () => {
    return api.get('/users/me/registrations');
  },

  getMyBookmarks: () => {
    return api.get('/users/me/bookmarks');
  },

  getNotifications: () => {
    return api.get('/notifications');
  },

  markNotificationRead: (id) => {
    return api.put(`/notifications/${id}/read`, {});
  },
};

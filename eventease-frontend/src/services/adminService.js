import { api } from './api';

export const adminService = {
  getDashboard: () => {
    return api.get('/admin/dashboard');
  },

  getAdminEvents: () => {
    return api.get('/admin/events');
  },

  getAdminUsers: () => {
    return api.get('/admin/users');
  },
};

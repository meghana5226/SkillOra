import api from './api'

export const userService = {
  discover: (params) => api.get('/users', { params }).then((r) => r.data),
  getById: (id) => api.get(`/users/${id}`).then((r) => r.data),
  updateMe: (data) => api.put('/users/me', data).then((r) => r.data),
  addOfferedSkill: (data) => api.post('/users/me/offered-skills', data).then((r) => r.data),
  addWantedSkill: (data) => api.post('/users/me/wanted-skills', data).then((r) => r.data),
  removeOfferedSkill: (id) => api.delete(`/users/me/offered-skills/${id}`),
  removeWantedSkill: (id) => api.delete(`/users/me/wanted-skills/${id}`),
}

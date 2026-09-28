import api from './api'

export const skillService = {
  list: (category) => api.get('/skills', { params: category ? { category } : {} }).then((r) => r.data),
  create: (data) => api.post('/skills', data).then((r) => r.data),
  update: (id, data) => api.put(`/skills/${id}`, data).then((r) => r.data),
  remove: (id) => api.delete(`/skills/${id}`),
}

export const matchService = {
  myMatches: (limit = 10) => api.get('/matches', { params: { limit } }).then((r) => r.data),
}

export const swapService = {
  create: (data) => api.post('/swaps', data).then((r) => r.data),
  sent: (params) => api.get('/swaps/sent', { params }).then((r) => r.data),
  received: (params) => api.get('/swaps/received', { params }).then((r) => r.data),
  accept: (id) => api.put(`/swaps/${id}/accept`).then((r) => r.data),
  reject: (id) => api.put(`/swaps/${id}/reject`).then((r) => r.data),
  cancel: (id) => api.put(`/swaps/${id}/cancel`).then((r) => r.data),
}

export const sessionService = {
  create: (data) => api.post('/sessions', data).then((r) => r.data),
  upcoming: () => api.get('/sessions/upcoming').then((r) => r.data),
  history: () => api.get('/sessions/history').then((r) => r.data),
  confirm: (id) => api.put(`/sessions/${id}/confirm`).then((r) => r.data),
  complete: (id) => api.put(`/sessions/${id}/complete`).then((r) => r.data),
  cancel: (id) => api.put(`/sessions/${id}/cancel`).then((r) => r.data),
}

export const messageService = {
  listConversations: () => api.get('/conversations').then((r) => r.data),
  startWith: (userId) => api.post(`/conversations/with/${userId}`).then((r) => r.data),
  getMessages: (conversationId) => api.get(`/conversations/${conversationId}/messages`).then((r) => r.data),
  send: (conversationId, content) =>
    api.post(`/conversations/${conversationId}/messages`, { content }).then((r) => r.data),
}

export const reviewService = {
  create: (data) => api.post('/reviews', data).then((r) => r.data),
  forUser: (userId, params) => api.get(`/users/${userId}/reviews`, { params }).then((r) => r.data),
}

export const notificationService = {
  list: (params) => api.get('/notifications', { params }).then((r) => r.data),
  unreadCount: () => api.get('/notifications/unread-count').then((r) => r.data),
  markRead: (id) => api.put(`/notifications/${id}/read`),
  markAllRead: () => api.put('/notifications/read-all'),
}

export const goalService = {
  list: () => api.get('/goals').then((r) => r.data),
  create: (data) => api.post('/goals', data).then((r) => r.data),
  update: (id, data) => api.put(`/goals/${id}`, data).then((r) => r.data),
}

export const adminService = {
  dashboard: () => api.get('/admin/dashboard').then((r) => r.data),
  users: (params) => api.get('/admin/users', { params }).then((r) => r.data),
  setUserStatus: (id, active) => api.put(`/admin/users/${id}/status`, { active }).then((r) => r.data),
}

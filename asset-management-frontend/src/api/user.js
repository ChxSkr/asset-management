import request from './request'

export function login(data) {
  return request.post('/user/login', data)
}

export function register(data) {
  return request.post('/user/register', data)
}

export function getProfile() {
  return request.get('/user/profile')
}

export function updateProfile(data) {
  return request.put('/user/profile', data)
}

export function getUserList(params) {
  return request.get('/user/list', { params })
}

export function createUser(data) {
  return request.post('/user/create', data)
}

export function getUserById(userId) {
  return request.get(`/user/${userId}`)
}

export function updateUser(userId, data) {
  return request.put(`/user/${userId}`, data)
}

export function resetPassword(userId, newPassword) {
  return request.put(`/user/${userId}/password`, { newPassword })
}
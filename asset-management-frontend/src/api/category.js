import request from './request'

export function getTree() {
  return request.get('/category/tree')
}

export function add(data) {
  return request.post('/category', data)
}

export function update(id, data) {
  return request.put(`/category/${id}`, data)
}

export function remove(id) {
  return request.delete(`/category/${id}`)
}
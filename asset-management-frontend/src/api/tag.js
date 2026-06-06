import request from './request'

export function getList() {
  return request.get('/tag/list')
}

export function add(data) {
  return request.post('/tag', data)
}

export function remove(id) {
  return request.delete(`/tag/${id}`)
}
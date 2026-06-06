import request from './request'

export function upload(formData) {
  return request.post('/asset/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function getList(params) {
  return request.get('/asset/list', { params })
}

export function getDetail(id) {
  return request.get(`/asset/${id}`)
}

export function update(id, data) {
  return request.put(`/asset/${id}`, data)
}

export function deleteAsset(id) {
  return request.delete(`/asset/${id}`)
}

export function download(id) {
  return request.get(`/asset/${id}/download`, { responseType: 'blob' })
}

export function restore(id) {
  return request.put(`/asset/${id}/restore`)
}

export function permanentDelete(id) {
  return request.delete(`/asset/${id}/permanent`)
}

export function getRecycleList(params) {
  return request.get('/asset/recycle', { params })
}

export function uploadVersion(id, formData) {
  return request.post(`/asset/${id}/version`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function getVersions(id) {
  return request.get(`/asset/${id}/versions`)
}

export function downloadVersion(versionId) {
  return request.get(`/asset/version/download/${versionId}`, { responseType: 'blob' })
}
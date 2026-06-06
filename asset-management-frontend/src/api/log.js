import request from './request'

export function getOperationLogs(params) {
  return request.get('/log/operation', { params })
}

export function getDownloadLogs(params) {
  return request.get('/log/download', { params })
}
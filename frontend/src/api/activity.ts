import request from '../utils/request'

export function getActivities(params: Record<string, unknown>) {
  return request.get('/activities', { params }) as unknown as Promise<any>
}

export function getActivity(id: string | number) {
  return request.get(`/activities/${id}`) as unknown as Promise<any>
}

export function createActivity(data: Record<string, unknown>) {
  return request.post('/activities', data) as unknown as Promise<any>
}

export function updateActivity(id: string | number, data: Record<string, unknown>) {
  return request.put(`/activities/${id}`, data) as unknown as Promise<any>
}

export function submitActivity(id: string | number) {
  return request.post(`/activities/${id}/submit`) as unknown as Promise<any>
}

export function reviewActivity(id: string | number, data: { result: string; reason?: string }) {
  return request.post(`/activities/${id}/review`, data) as unknown as Promise<any>
}

export function cancelActivity(id: string | number) {
  return request.post(`/activities/${id}/cancel`, {}) as unknown as Promise<any>
}

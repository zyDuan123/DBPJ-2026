import request from '../utils/request'

export function getCampuses() {
  return request.get('/campuses') as unknown as Promise<any>
}

export function getVenues(params: Record<string, unknown> = {}) {
  return request.get('/venues', { params }) as unknown as Promise<any>
}

export function getCategories() {
  return request.get('/categories') as unknown as Promise<any>
}

export function createCampus(data: Record<string, unknown>) {
  return request.post('/campuses', data) as unknown as Promise<any>
}

export function createVenue(data: Record<string, unknown>) {
  return request.post('/venues', data) as unknown as Promise<any>
}

export function createCategory(data: Record<string, unknown>) {
  return request.post('/categories', data) as unknown as Promise<any>
}

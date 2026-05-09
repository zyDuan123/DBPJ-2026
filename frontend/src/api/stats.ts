import request from '../utils/request'

export function getOverview() {
  return request.get('/stats/overview') as unknown as Promise<any>
}

export function getCampusUsage() {
  return request.get('/stats/campus-usage') as unknown as Promise<any>
}

export function getCategoryPopularity() {
  return request.get('/stats/category-popularity') as unknown as Promise<any>
}

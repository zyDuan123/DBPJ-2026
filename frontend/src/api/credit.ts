import request from '../utils/request'

export function getMyCredit(params: Record<string, unknown>) {
  return request.get('/credit/my', { params }) as unknown as Promise<any>
}

export function getCreditOverview() {
  return request.get('/credit/overview') as unknown as Promise<any>
}

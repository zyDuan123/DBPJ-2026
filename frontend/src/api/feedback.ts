import request from '../utils/request'

export function submitFeedback(activityId: string | number, data: { rating: number; content?: string }) {
  return request.post(`/activities/${activityId}/feedback`, data) as unknown as Promise<any>
}

export function getMyFeedback(activityId: string | number) {
  return request.get(`/activities/${activityId}/feedback/my`) as unknown as Promise<any>
}

export function getActivityFeedback(activityId: string | number, params: Record<string, unknown>) {
  return request.get(`/activities/${activityId}/feedback`, { params }) as unknown as Promise<any>
}

export function getFeedbackOverview() {
  return request.get('/feedback/overview') as unknown as Promise<any>
}

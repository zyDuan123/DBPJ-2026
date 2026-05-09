import request from '../utils/request'

export function enroll(activityId: string | number) {
  return request.post(`/activities/${activityId}/registrations`) as unknown as Promise<any>
}

export function cancelRegistration(registrationId: string | number) {
  return request.delete(`/registrations/${registrationId}`) as unknown as Promise<any>
}

export function getMyRegistrations(params: Record<string, unknown>) {
  return request.get('/registrations/my', { params }) as unknown as Promise<any>
}

export function getActivityRegistrations(activityId: string | number, params: Record<string, unknown>) {
  return request.get(`/activities/${activityId}/registrations`, { params }) as unknown as Promise<any>
}

export function getCheckInCode(registrationId: string | number) {
  return request.get(`/registrations/${registrationId}/check-in-code`) as unknown as Promise<any>
}

export function checkIn(checkInCode: string) {
  return request.patch('/registrations/check-in', { checkInCode }) as unknown as Promise<any>
}

export function markAbsences(activityId: string | number) {
  return request.post(`/activities/${activityId}/registrations/absences`) as unknown as Promise<any>
}

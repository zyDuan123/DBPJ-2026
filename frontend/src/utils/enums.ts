export const ActivityStatusText: Record<string, string> = {
  DRAFT: '草稿',
  PENDING_REVIEW: '待审核',
  REJECTED: '已驳回',
  PUBLISHED: '已发布',
  ONGOING: '进行中',
  FINISHED: '已结束',
  CANCELLED: '已取消',
}

export const RegistrationStatusText: Record<string, string> = {
  ENROLLED: '正选',
  WAITLISTED: '候补',
  CANCELLED: '已取消',
  CHECKED_IN: '已签到',
  ABSENT: '未签到',
}

export function statusTag(status?: string) {
  const map: Record<string, string> = {
    DRAFT: 'info',
    PENDING_REVIEW: 'warning',
    REJECTED: 'danger',
    PUBLISHED: 'success',
    ONGOING: 'primary',
    FINISHED: 'info',
    CANCELLED: 'info',
    ENROLLED: 'success',
    WAITLISTED: 'warning',
    CHECKED_IN: 'primary',
    ABSENT: 'danger',
  }
  return status ? map[status] || 'info' : 'info'
}

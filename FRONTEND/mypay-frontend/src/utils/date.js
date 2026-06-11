import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
dayjs.extend(relativeTime)

export function formatDate(iso) {
  if (!iso) return '—'
  return dayjs(iso).format('DD MMM YYYY, HH:mm')
}

export function fromNow(iso) {
  if (!iso) return ''
  return dayjs(iso).fromNow()
}

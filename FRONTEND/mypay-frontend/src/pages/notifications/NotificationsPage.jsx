import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import PageLayout from '../../components/layout/PageLayout'
import Button from '../../components/ui/Button'
import Modal from '../../components/ui/Modal'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import EmptyState from '../../components/ui/EmptyState'
import { fromNow } from '../../utils/date'
import { listNotifications, markRead, markAllRead } from '../../api/notification'
import { myInvitations, respond } from '../../api/invitation'
import { getApiErrorMessage } from '../../utils/apiError'

export default function NotificationsPage() {
  const qc = useQueryClient()
  const [selectedInvitation, setSelectedInvitation] = useState(null)
  const [invitationError, setInvitationError] = useState('')

  const { data, isLoading } = useQuery({
    queryKey: ['notifications'],
    queryFn: listNotifications,
  })
  const notifications = data?.data ?? []

  const { data: invitationsData, isLoading: invitationsLoading } = useQuery({
    queryKey: ['invitations', 'mine'],
    queryFn: myInvitations,
  })
  const invitations = invitationsData?.data ?? []

  const markOneMut = useMutation({
    mutationFn: (id) => markRead(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['notifications'] })
      qc.invalidateQueries({ queryKey: ['notificationCount'] })
    },
  })

  const markAllMut = useMutation({
    mutationFn: markAllRead,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['notifications'] })
      qc.invalidateQueries({ queryKey: ['notificationCount'] })
    },
  })

  const respondMut = useMutation({
    mutationFn: ({ id, action }) => respond(id, action),
    onSuccess: async (res, variables) => {
      const accepted = variables.action === 'ACCEPT'
      if (selectedInvitation?.notificationId) {
        await markRead(selectedInvitation.notificationId)
        qc.setQueryData(['notifications'], (current) => {
          const items = current?.data ?? []
          return {
            data: items.map((item) => {
              const itemId = item.notificationId ?? item.id
              if (itemId !== selectedInvitation.notificationId) return item
              const actionText = accepted ? 'accepted' : 'rejected'
              return {
                ...item,
                type: accepted ? 'INVITATION_ACCEPTED_CONFIRMATION' : 'INVITATION_DECLINED_CONFIRMATION',
                title: accepted ? 'Invitation accepted' : 'Invitation rejected',
                message: `You ${actionText} ${selectedInvitation.inviterName ?? selectedInvitation.inviterId}'s invitation to ${selectedInvitation.collectionName ?? selectedInvitation.collectionId}.`,
                read: true,
              }
            }),
          }
        })
      }
      qc.invalidateQueries({ queryKey: ['invitations'] })
      qc.invalidateQueries({ queryKey: ['collections'] })
      qc.invalidateQueries({ queryKey: ['notificationCount'] })
      setSelectedInvitation(null)
      setInvitationError('')
    },
    onError: (err) => setInvitationError(getApiErrorMessage(err, 'Failed to update invitation')),
  })

  const handleNotificationClick = (notification) => {
    const notificationId = notification.notificationId ?? notification.id
    if (notification.type !== 'INVITATION_RECEIVED') {
      if (!notification.read) {
        markOneMut.mutate(notificationId)
      }
      return
    }

    if (invitationsLoading) {
      return
    }

    const invitation = invitations.find((inv) => inv.invitationId === notification.referenceId)
    if (!invitation || invitation.status !== 'PENDING') {
      if (!notification.read) {
        markOneMut.mutate(notificationId)
      }
      return
    }

    setInvitationError('')
    setSelectedInvitation({ ...invitation, notificationId, notificationRead: notification.read })
  }

  const ignoreInvitation = () => {
    if (selectedInvitation?.notificationId && !selectedInvitation.notificationRead) {
      markOneMut.mutate(selectedInvitation.notificationId)
    }
    setSelectedInvitation(null)
    setInvitationError('')
  }

  const unreadCount = notifications.filter((n) => !n.read).length

  return (
    <PageLayout
      title="Notifications"
      back
      actions={
        unreadCount > 0 && (
          <button
            onClick={() => markAllMut.mutate()}
            className="text-xs text-primary font-medium"
          >
            Mark all read
          </button>
        )
      }
    >
      <div className="px-4 py-3">
        {isLoading ? (
          <LoadingSpinner fullPage />
        ) : notifications.length === 0 ? (
          <EmptyState icon="🔔" title="No notifications" description="You're all caught up!" />
        ) : (
          <div className="space-y-1">
            {notifications.map((n) => (
              <button
                key={n.notificationId ?? n.id}
                onClick={() => handleNotificationClick(n)}
                className={`w-full text-left px-4 py-3 rounded-xl transition-colors ${
                  n.read ? 'bg-white' : 'bg-primary/5 hover:bg-primary/10'
                }`}
              >
                <div className="flex items-start gap-3">
                  {!n.read && (
                    <div className="mt-1.5 w-2 h-2 rounded-full bg-primary shrink-0" />
                  )}
                  <div className={`flex-1 min-w-0 ${n.read ? 'pl-5' : ''}`}>
                    <p className="text-sm font-medium text-gray-800">{n.title ?? n.type}</p>
                    {n.message && <p className="text-xs text-gray-500 mt-0.5 leading-relaxed">{n.message}</p>}
                  </div>
                  <p className="text-[10px] text-gray-400 shrink-0 pt-0.5">{fromNow(n.createdAt)}</p>
                </div>
              </button>
            ))}
          </div>
        )}
      </div>

      <Modal
        open={!!selectedInvitation}
        onClose={() => {
          setSelectedInvitation(null)
          setInvitationError('')
        }}
        title="Collection invitation"
      >
        {selectedInvitation && (
          <div className="space-y-4">
            <div>
              <p className="text-sm font-semibold text-gray-900">
                {selectedInvitation.collectionName ?? selectedInvitation.collectionId}
              </p>
              <p className="text-xs text-gray-500 mt-1">
                Invited by {selectedInvitation.inviterName ?? selectedInvitation.inviterId}
              </p>
              <p className="text-xs text-gray-500 mt-1">
                Currency: {selectedInvitation.collectionCurrency ?? selectedInvitation.currency ?? 'MYR'}
              </p>
              <p className="text-xs text-gray-500 mt-1">
                Role: {selectedInvitation.role}
              </p>
            </div>
            {invitationError && <p className="text-sm text-danger">{invitationError}</p>}
            <div className="grid grid-cols-3 gap-2">
              <Button
                size="sm"
                loading={respondMut.isPending}
                onClick={() => respondMut.mutate({ id: selectedInvitation.invitationId, action: 'ACCEPT' })}
              >
                Accept
              </Button>
              <Button
                size="sm"
                variant="secondary"
                disabled={respondMut.isPending}
                onClick={() => respondMut.mutate({ id: selectedInvitation.invitationId, action: 'DECLINE' })}
              >
                Reject
              </Button>
              <Button
                size="sm"
                variant="outline"
                disabled={respondMut.isPending}
                onClick={ignoreInvitation}
              >
                Ignore
              </Button>
            </div>
          </div>
        )}
      </Modal>
    </PageLayout>
  )
}

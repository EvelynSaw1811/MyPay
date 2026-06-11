import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate, useSearchParams } from 'react-router-dom'
import PageLayout from '../../components/layout/PageLayout'
import Button from '../../components/ui/Button'
import Badge from '../../components/ui/Badge'
import Modal from '../../components/ui/Modal'
import Input from '../../components/ui/Input'
import Select from '../../components/ui/Select'
import EmptyState from '../../components/ui/EmptyState'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import { formatDate } from '../../utils/date'
import { myInvitations, respond, sendInvitation } from '../../api/invitation'
import { getApiErrorMessage } from '../../utils/apiError'

const ROLES = [
  { value: 'EDITOR', label: 'Editor' },
  { value: 'MEMBER', label: 'Member' },
]

const statusColor = { PENDING: 'yellow', ACCEPTED: 'green', DECLINED: 'red', EXPIRED: 'gray' }

export default function InvitationsPage() {
  const qc = useQueryClient()
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const colId = searchParams.get('colId')
  const [showInvite, setShowInvite] = useState(!!colId)
  const [inviteForm, setInviteForm] = useState({ invitationCode: '', role: 'MEMBER', collectionId: colId ?? '' })
  const [inviteError, setInviteError] = useState('')

  const { data, isLoading } = useQuery({
    queryKey: ['invitations', 'mine'],
    queryFn: myInvitations,
  })
  const invitations = data?.data ?? []
  const pending = invitations.filter((i) => i.status === 'PENDING')

  const respondMut = useMutation({
    mutationFn: ({ id, action }) => respond(id, action),
    onSuccess: (res) => {
      qc.invalidateQueries({ queryKey: ['invitations'] })
      qc.invalidateQueries({ queryKey: ['collections'] })
      qc.invalidateQueries({ queryKey: ['notifications'] })
      qc.invalidateQueries({ queryKey: ['notificationCount'] })
      if (res?.status === 'ACCEPTED' && res?.collectionId) {
        navigate(`/app/collections/${res.collectionId}`)
      }
    },
  })

  const sendMut = useMutation({
    mutationFn: () => sendInvitation(inviteForm.collectionId, {
      identifier: inviteForm.invitationCode,
      role: inviteForm.role,
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['invitations'] })
      qc.invalidateQueries({ queryKey: ['collection', inviteForm.collectionId, 'invitations'] })
      setShowInvite(false)
      setInviteForm({ invitationCode: '', role: 'MEMBER', collectionId: colId ?? '' })
      setInviteError('')
    },
    onError: (err) => setInviteError(getApiErrorMessage(err, 'Failed to send invitation')),
  })

  return (
    <PageLayout
      title="Invitations"
      back
      actions={
        <Button size="sm" onClick={() => setShowInvite(true)}>
          + Invite
        </Button>
      }
    >
      <div className="px-4 py-3">
        {isLoading ? (
          <LoadingSpinner fullPage />
        ) : pending.length === 0 ? (
          <EmptyState icon="✉️" title="No pending invitations" />
        ) : (
          <div className="space-y-3">
            {pending.map((inv) => (
              <div key={inv.invitationId} className="bg-white border border-gray-100 rounded-2xl p-4 shadow-sm">
                <div className="flex items-start justify-between mb-3">
                  <div>
                    <p className="text-sm font-semibold text-gray-900">{inv.collectionName ?? inv.collectionId}</p>
                    <p className="text-xs text-gray-400">
                      Invited by {inv.inviterName ?? inv.inviterId} · {formatDate(inv.createdAt)}
                    </p>
                  </div>
                  <Badge color={statusColor[inv.status] ?? 'gray'}>{inv.status}</Badge>
                </div>
                <div className="flex gap-2">
                  <Button
                    size="sm"
                    className="flex-1"
                    loading={respondMut.isPending}
                    onClick={() => respondMut.mutate({ id: inv.invitationId, action: 'ACCEPT' })}
                  >
                    Accept
                  </Button>
                  <Button
                    size="sm"
                    variant="secondary"
                    className="flex-1"
                    onClick={() => respondMut.mutate({ id: inv.invitationId, action: 'DECLINE' })}
                  >
                    Decline
                  </Button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      <Modal open={showInvite} onClose={() => { setShowInvite(false); setInviteError('') }} title="Send invitation">
        <div className="space-y-4">
          {!colId && (
            <Input
              label="Collection ID"
              value={inviteForm.collectionId}
              onChange={(e) => setInviteForm({ ...inviteForm, collectionId: e.target.value })}
              placeholder="Collection ID"
            />
          )}
          <Input
            label="Invitation code"
            value={inviteForm.invitationCode}
            onChange={(e) => setInviteForm({ ...inviteForm, invitationCode: e.target.value })}
            placeholder="MP-00000001"
          />
          <Select
            label="Role"
            value={inviteForm.role}
            onChange={(e) => setInviteForm({ ...inviteForm, role: e.target.value })}
            options={ROLES}
          />
          {inviteError && <p className="text-sm text-danger">{inviteError}</p>}
          <Button
            className="w-full"
            loading={sendMut.isPending}
            onClick={() => sendMut.mutate()}
            disabled={!inviteForm.invitationCode.trim() || !inviteForm.collectionId.trim()}
          >
            Send invitation
          </Button>
        </div>
      </Modal>
    </PageLayout>
  )
}

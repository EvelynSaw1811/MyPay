import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import PageLayout from '../../components/layout/PageLayout'
import Button from '../../components/ui/Button'
import Input from '../../components/ui/Input'
import Modal from '../../components/ui/Modal'
import EmptyState from '../../components/ui/EmptyState'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import { getPayees, addPayee, removePayee } from '../../api/payee'
import { getApiErrorMessage } from '../../utils/apiError'

export default function PayeesPage() {
  const qc = useQueryClient()
  const [showAdd, setShowAdd] = useState(false)
  const [identifier, setIdentifier] = useState('')
  const [nickname, setNickname] = useState('')
  const [addError, setAddError] = useState('')

  const { data, isLoading } = useQuery({ queryKey: ['payees'], queryFn: getPayees })
  const payees = data?.data ?? []

  const addMutation = useMutation({
    mutationFn: () => addPayee({ identifier, nickname }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['payees'] })
      setShowAdd(false)
      setIdentifier('')
      setNickname('')
      setAddError('')
    },
    onError: (err) => setAddError(getApiErrorMessage(err, 'Failed to add payee')),
  })

  const removeMutation = useMutation({
    mutationFn: (id) => removePayee(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['payees'] }),
  })

  return (
    <PageLayout
      title="Payees"
      actions={
        <Button size="sm" onClick={() => setShowAdd(true)}>
          + Add
        </Button>
      }
    >
      <div className="px-4 py-3">
        {isLoading ? (
          <LoadingSpinner fullPage />
        ) : payees.length === 0 ? (
          <EmptyState
            icon="👥"
            title="No payees yet"
            description="Add someone to send money to"
            action={
              <Button onClick={() => setShowAdd(true)}>Add payee</Button>
            }
          />
        ) : (
          <div className="space-y-2">
            {payees.map((p) => (
              <div key={p.payeeId ?? p.id} className="flex items-center gap-3 py-3 border-b border-gray-50 last:border-0">
                <div className="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center text-sm font-bold text-primary">
                  {(p.nickname ?? p.userId ?? '?')[0].toUpperCase()}
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-gray-800 truncate">{p.nickname ?? p.userId}</p>
                  <p className="text-xs text-gray-400 truncate">{p.userId}</p>
                </div>
                <button
                  onClick={() => removeMutation.mutate(p.payeeId ?? p.id)}
                  className="text-danger text-sm hover:text-red-700"
                >
                  Remove
                </button>
              </div>
            ))}
          </div>
        )}
      </div>

      <Modal open={showAdd} onClose={() => { setShowAdd(false); setIdentifier(''); setNickname(''); setAddError('') }} title="Add payee">
        <div className="space-y-4">
          <Input
            label="User ID, email, or phone"
            value={identifier}
            onChange={(e) => setIdentifier(e.target.value)}
            placeholder="Enter identifier"
          />
          <Input
            label="Nickname"
            value={nickname}
            onChange={(e) => setNickname(e.target.value)}
            placeholder="Display name"
          />
          {addError && <p className="text-sm text-danger">{addError}</p>}
          <Button
            className="w-full"
            loading={addMutation.isPending}
            onClick={() => addMutation.mutate()}
            disabled={!identifier.trim()}
          >
            Add payee
          </Button>
        </div>
      </Modal>
    </PageLayout>
  )
}

import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import PageLayout from '../../components/layout/PageLayout'
import Input from '../../components/ui/Input'
import Select from '../../components/ui/Select'
import Button from '../../components/ui/Button'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import { getCollection, getMembers, updateCollection, closeCollection, removeMember } from '../../api/collection'
import { getApiErrorMessage } from '../../utils/apiError'

const CATEGORIES = [
  { value: 'TRIP', label: 'Trip' },
  { value: 'EXPENSE', label: 'Expense' },
  { value: 'MONTHLY', label: 'Monthly' },
  { value: 'OTHER', label: 'Other' },
]

export default function CollectionSettingsPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [saveError, setSaveError] = useState('')

  const { data: colData, isLoading } = useQuery({
    queryKey: ['collection', id],
    queryFn: () => getCollection(id),
  })
  const { data: membersData } = useQuery({
    queryKey: ['collection', id, 'members'],
    queryFn: () => getMembers(id),
  })

  const col = colData?.data
  const members = membersData?.data ?? []

  const [form, setForm] = useState(null)
  if (col && !form) {
    setForm({ name: col.name, category: col.category, typeName: col.typeName ?? col.category, currency: col.currency ?? 'MYR', description: col.description ?? '' })
  }

  const updateMut = useMutation({
    mutationFn: () => updateCollection(id, form),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['collection', id] })
      qc.invalidateQueries({ queryKey: ['collections'] })
      setSaveError('')
    },
    onError: (err) => setSaveError(getApiErrorMessage(err, 'Save failed')),
  })

  const closeMut = useMutation({
    mutationFn: () => closeCollection(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['collection', id] })
      qc.invalidateQueries({ queryKey: ['collections'] })
      navigate(`/app/collections/${id}`, { replace: true })
    },
  })

  const removeMemberMut = useMutation({
    mutationFn: (uid) => removeMember(id, uid),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['collection', id, 'members'] }),
  })

  if (isLoading || !form) return <PageLayout title="Settings" back><LoadingSpinner fullPage /></PageLayout>

  return (
    <PageLayout title="Collection Settings" back>
      <div className="px-4 py-5 space-y-6">
        {/* Edit metadata */}
        <div className="space-y-4">
          <p className="text-sm font-semibold text-gray-700">Details</p>
          <Input label="Name" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
          <Select
            label="Category"
            value={form.category}
            onChange={(e) => setForm({ ...form, category: e.target.value })}
            options={CATEGORIES}
          />
          <Input
            label="Type name"
            value={form.typeName}
            onChange={(e) => setForm({ ...form, typeName: e.target.value })}
          />
          <Input
            label="Description"
            value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
          />
          <Select
            label="Currency"
            value={form.currency}
            onChange={(e) => setForm({ ...form, currency: e.target.value })}
            options={['MYR', 'SGD', 'USD'].map((currency) => ({ value: currency, label: currency }))}
          />
          {saveError && <p className="text-sm text-danger">{saveError}</p>}
          <Button className="w-full" loading={updateMut.isPending} onClick={() => updateMut.mutate()}>
            Save changes
          </Button>
        </div>

        {/* Members management */}
        <div>
          <p className="text-sm font-semibold text-gray-700 mb-3">Members</p>
          <div className="space-y-2">
            {members.map((m) => (
              <div key={m.userId} className="flex items-center gap-3">
                <div className="flex-1 text-sm text-gray-800">{m.userNickname ?? m.name ?? m.userId}</div>
                <span className="text-xs text-gray-400">{m.role}</span>
                {m.role !== 'ADMIN' && (
                  <button
                    onClick={() => removeMemberMut.mutate(m.userId)}
                    className="text-sm text-danger hover:text-red-700"
                  >
                    Remove
                  </button>
                )}
              </div>
            ))}
          </div>
        </div>

        {/* Close collection */}
        {col.status === 'ACTIVE' && (
          <div className="border-t border-gray-100 pt-4">
            <p className="text-xs text-gray-400 mb-3">Closing a collection is permanent and cannot be undone.</p>
            <Button
              variant="danger"
              className="w-full"
              loading={closeMut.isPending}
              onClick={() => {
                if (window.confirm('Close this collection? This cannot be undone.')) closeMut.mutate()
              }}
            >
              Close collection
            </Button>
          </div>
        )}
      </div>
    </PageLayout>
  )
}

import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import PageLayout from '../../components/layout/PageLayout'
import Input from '../../components/ui/Input'
import Select from '../../components/ui/Select'
import Button from '../../components/ui/Button'
import Card from '../../components/ui/Card'
import { createCollection, listCollectionTypes } from '../../api/collection'
import { sendInvitation } from '../../api/invitation'
import { getWallet } from '../../api/wallet'
import { getApiErrorMessage } from '../../utils/apiError'

const SYSTEM_CATEGORY_BY_NAME = {
  Trip: 'TRIP',
  Expense: 'EXPENSE',
  Monthly: 'MONTHLY',
  Other: 'OTHER',
}

export default function CreateCollectionPage() {
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [step, setStep] = useState(1)
  const [created, setCreated] = useState(null)
  const [form, setForm] = useState({ name: '', typeName: 'Expense', currency: '', description: '' })
  const [memberCode, setMemberCode] = useState('')
  const [members, setMembers] = useState([])
  const [role, setRole] = useState('MEMBER')
  const [error, setError] = useState('')
  const [inviteError, setInviteError] = useState('')

  const { data: typeData } = useQuery({
    queryKey: ['collection-types'],
    queryFn: listCollectionTypes,
  })
  const { data: walletData } = useQuery({
    queryKey: ['wallet'],
    queryFn: () => getWallet().then((r) => r.data),
  })
  const typeOptions = useMemo(() => {
    const types = typeData?.data ?? [
      { name: 'Trip', system: true },
      { name: 'Expense', system: true },
      { name: 'Monthly', system: true },
      { name: 'Other', system: true },
    ]
    return types.map((t) => ({ value: t.name, label: t.system ? t.name : `${t.name} (custom)` }))
  }, [typeData])
  const walletCurrencies = useMemo(() => {
    const wallets = walletData?.wallets ?? walletData?.accounts ?? []
    return wallets.map((item) => item.currency)
  }, [walletData])

  useEffect(() => {
    if (!form.currency && walletCurrencies.length > 0) {
      setForm((current) => ({ ...current, currency: walletCurrencies[0] }))
    }
  }, [form.currency, walletCurrencies])

  const createMut = useMutation({
    mutationFn: () => {
      const category = SYSTEM_CATEGORY_BY_NAME[form.typeName] ?? 'OTHER'
      return createCollection({ ...form, category })
    },
    onSuccess: (res) => {
      qc.invalidateQueries({ queryKey: ['collections'] })
      setCreated(res.data)
      setStep(2)
      setError('')
    },
    onError: (err) => setError(getApiErrorMessage(err, 'Failed to create collection')),
  })

  const inviteMut = useMutation({
    mutationFn: (payload) => sendInvitation(created.collectionId, payload),
    onSuccess: (_, payload) => {
      setMembers((items) => [...items, { invitationCode: payload.identifier, role: payload.role }])
      setMemberCode('')
      setInviteError('')
    },
    onError: (err) => setInviteError(getApiErrorMessage(err, 'Failed to invite member')),
  })

  function set(field) {
    return (e) => setForm((f) => ({ ...f, [field]: e.target.value }))
  }

  function finish() {
    qc.invalidateQueries({ queryKey: ['collections'] })
    if (created?.collectionId) navigate(`/app/collections/${created.collectionId}`, { replace: true })
  }

  return (
    <PageLayout title={step === 1 ? 'New Collection' : 'Add Members'} back>
      <div className="px-4 py-5 space-y-4">
        <div className="flex gap-2 text-xs font-medium">
          <span className={`flex-1 rounded-lg px-3 py-2 text-center ${step === 1 ? 'bg-primary text-white' : 'bg-gray-100 text-gray-500'}`}>Details</span>
          <span className={`flex-1 rounded-lg px-3 py-2 text-center ${step === 2 ? 'bg-primary text-white' : 'bg-gray-100 text-gray-500'}`}>Members</span>
        </div>

        {step === 1 ? (
          <form
            onSubmit={(e) => {
              e.preventDefault()
              setError('')
              createMut.mutate()
            }}
            className="space-y-4"
          >
            <Input label="Collection name" value={form.name} onChange={set('name')} required />
            <Select label="Type" value={form.typeName} onChange={set('typeName')} options={typeOptions} />
            <Select
              label="Currency"
              value={form.currency}
              onChange={set('currency')}
              options={walletCurrencies.map((currency) => ({ value: currency, label: currency }))}
            />
            <Input label="Description (optional)" value={form.description} onChange={set('description')} />
            {error && <p className="text-sm text-danger">{error}</p>}
            <Button type="submit" className="w-full" loading={createMut.isPending} disabled={!form.name.trim() || !form.currency}>
              Continue
            </Button>
          </form>
        ) : (
          <div className="space-y-4">
            <Card>
              <p className="text-sm font-semibold text-gray-900">{created?.name}</p>
              <p className="text-xs text-gray-400 mt-1">{created?.typeName ?? created?.category} · {created?.currency}</p>
            </Card>
            <div className="space-y-3">
              <Input
                label="Invitation code"
                value={memberCode}
                onChange={(e) => setMemberCode(e.target.value)}
                placeholder="MP-00000001"
              />
              <Select
                label="Role"
                value={role}
                onChange={(e) => setRole(e.target.value)}
                options={[
                  { value: 'MEMBER', label: 'Member' },
                  { value: 'EDITOR', label: 'Editor' },
                ]}
              />
              {inviteError && <p className="text-sm text-danger">{inviteError}</p>}
              <Button
                className="w-full"
                variant="outline"
                loading={inviteMut.isPending}
                disabled={!memberCode.trim()}
                onClick={() => inviteMut.mutate({ identifier: memberCode.trim(), role })}
              >
                Add member
              </Button>
            </div>

            {members.length > 0 && (
              <div className="space-y-2">
                {members.map((m) => (
                  <div key={`${m.invitationCode}-${m.role}`} className="flex justify-between rounded-lg bg-gray-50 px-3 py-2 text-sm">
                    <span>{m.invitationCode}</span>
                    <span className="text-gray-400">{m.role}</span>
                  </div>
                ))}
              </div>
            )}

            <div className="grid grid-cols-2 gap-2">
              <Button variant="secondary" onClick={finish}>Skip</Button>
              <Button onClick={finish}>Done</Button>
            </div>
          </div>
        )}
      </div>
    </PageLayout>
  )
}

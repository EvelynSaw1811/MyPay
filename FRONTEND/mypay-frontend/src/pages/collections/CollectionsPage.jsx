import { useMemo, useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import PageLayout from '../../components/layout/PageLayout'
import Card from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import Input from '../../components/ui/Input'
import Modal from '../../components/ui/Modal'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import EmptyState from '../../components/ui/EmptyState'
import { listCollections, listCollectionTypes, createCollectionType, deleteCollectionType } from '../../api/collection'
import { getApiErrorMessage } from '../../utils/apiError'
import { formatAmount } from '../../utils/currency'

const roleLabel   = { ADMIN: 'Admin', EDITOR: 'Editor', MEMBER: 'Member' }

const ALL_TAB = '__ALL__'

const ROLE_FILTERS   = ['ADMIN', 'EDITOR', 'MEMBER']
const STATUS_FILTERS = ['ACTIVE', 'CLOSED']
export default function CollectionsPage() {
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [activeTab, setActiveTab] = useState(ALL_TAB)
  const [roleFilter,   setRoleFilter]   = useState(null)   // null = no filter
  const [statusFilter, setStatusFilter] = useState('ACTIVE')
  const [showNewCategory, setShowNewCategory] = useState(false)
  const [newCategoryName, setNewCategoryName] = useState('')
  const [categoryError, setCategoryError] = useState('')
  const [orderedTypeIds, setOrderedTypeIds] = useState(() => {
    try { return JSON.parse(localStorage.getItem('collectionTypeOrder') ?? '[]') } catch { return [] }
  })

  const { data, isLoading } = useQuery({
    queryKey: ['collections'],
    queryFn: listCollections,
  })
  const { data: typeData } = useQuery({
    queryKey: ['collection-types'],
    queryFn: listCollectionTypes,
  })

  const collections = data?.data ?? []

  /**
   * Build the tab list:
   *   ALL → system types → custom (non-system) types
   * Custom types come from /api/collections/types where `system === false`.
   */
  const tabs = useMemo(() => {
    const types = typeData?.data ?? []
    const orderedTypes = [...types].sort((a, b) => {
      const ai = orderedTypeIds.indexOf(a.collectionTypeId)
      const bi = orderedTypeIds.indexOf(b.collectionTypeId)
      if (ai === -1 && bi === -1) return 0
      if (ai === -1) return 1
      if (bi === -1) return -1
      return ai - bi
    })
    return [
      { name: ALL_TAB, label: 'ALL' },
      ...orderedTypes.map((t) => ({ id: t.collectionTypeId, name: t.name, label: t.name.toUpperCase() })),
    ]
  }, [orderedTypeIds, typeData])

  const filtered = useMemo(() => {
    return collections.filter((c) => {
      const typeMatch = activeTab === ALL_TAB ||
        (c.typeName ?? c.category ?? '').toString().toLowerCase() === activeTab.toLowerCase()
      const roleMatch   = !roleFilter   || (c.myRole ?? 'MEMBER').toUpperCase() === roleFilter
      const statusMatch = !statusFilter || (c.status ?? 'ACTIVE').toUpperCase() === statusFilter
      return typeMatch && roleMatch && statusMatch
    })
  }, [collections, activeTab, roleFilter, statusFilter])

  const typeMut = useMutation({
    mutationFn: () => createCollectionType({ name: newCategoryName.trim() }),
    onSuccess: (res) => {
      qc.invalidateQueries({ queryKey: ['collection-types'] })
      setNewCategoryName('')
      setCategoryError('')
      setShowNewCategory(false)
      // Switch to the freshly-created tab so the user can see it took effect.
      if (res?.data?.name) setActiveTab(res.data.name)
    },
    onError: (err) => setCategoryError(getApiErrorMessage(err, 'Failed to create category')),
  })
  const deleteTypeMut = useMutation({
    mutationFn: deleteCollectionType,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['collection-types'] }),
  })

  function reorderTabs(draggedId, targetId) {
    if (!draggedId || !targetId || draggedId === targetId) return
    const typeIds = (typeData?.data ?? []).map((type) => type.collectionTypeId)
    const current = orderedTypeIds.length > 0 ? [...orderedTypeIds] : typeIds
    const next = current.filter((id) => id !== draggedId)
    const targetIndex = next.indexOf(targetId)
    next.splice(targetIndex, 0, draggedId)
    setOrderedTypeIds(next)
    localStorage.setItem('collectionTypeOrder', JSON.stringify(next))
  }

  return (
    <PageLayout title="Collections">
      {/* Filter tabs (horizontally scrollable) — + button sits right after the last tab */}
      <div className="px-4 pt-3">
        <div className="border-b border-gray-100">
          <div className="flex items-center">
            {/* Horizontally-scrollable tab strip */}
            <div className="flex-1 min-w-0 flex gap-1 overflow-x-auto no-scrollbar">
              {tabs.map((t) => (
                <button
                  key={t.name}
                  onClick={() => setActiveTab(t.name)}
                  draggable={t.name !== ALL_TAB}
                  onDragStart={(e) => t.id && e.dataTransfer.setData('text/plain', t.id)}
                  onDragOver={(e) => t.id && e.preventDefault()}
                  onDrop={(e) => {
                    e.preventDefault()
                    reorderTabs(e.dataTransfer.getData('text/plain'), t.id)
                  }}
                  className={`h-8 whitespace-nowrap px-3 text-xs font-semibold tracking-wide border-b-2 -mb-px transition-colors ${
                    activeTab === t.name
                      ? 'border-primary text-primary'
                      : 'border-transparent text-gray-500 hover:text-gray-700'
                  }`}
                >
                  {t.label}
                </button>
              ))}
            </div>
            {/* + button — always pinned to the far right, outside the scroll area */}
            <button
              onClick={() => { setCategoryError(''); setShowNewCategory(true) }}
              aria-label="Add new category"
              className="shrink-0 ml-2 h-6 w-6 rounded-md border border-gray-300 text-gray-500 text-base flex items-center justify-center hover:border-primary hover:text-primary transition-colors"
            >
              +
            </button>
          </div>
        </div>
      </div>

      <div className="px-4 py-3">
        {/* Role + status quick-filter chips */}
        <div className="grid grid-cols-5 gap-1.5 mb-3">
          {ROLE_FILTERS.map((r) => (
            <button
              key={r}
              onClick={() => setRoleFilter(roleFilter === r ? null : r)}
              className={`px-0 py-1 rounded-full text-[11px] font-semibold border transition-colors text-center ${
                roleFilter === r
                  ? r === 'ADMIN'  ? 'bg-purple-600 text-white border-purple-600'
                  : r === 'EDITOR' ? 'bg-blue-600 text-white border-blue-600'
                  :                  'bg-gray-600 text-white border-gray-600'
                  : 'bg-white text-gray-500 border-gray-200 hover:border-gray-400'
              }`}
            >
              {r}
            </button>
          ))}
          {STATUS_FILTERS.map((status) => (
            <button
              key={status}
              onClick={() => setStatusFilter(statusFilter === status ? null : status)}
              className={`px-0 py-1 rounded-full text-[11px] font-semibold border transition-colors text-center ${
                statusFilter === status
                  ? status === 'ACTIVE'
                    ? 'bg-green-600 text-white border-green-600'
                    : 'bg-gray-600 text-white border-gray-600'
                  : 'bg-white text-gray-500 border-gray-200 hover:border-gray-400'
              }`}
            >
              {status}
            </button>
          ))}
        </div>

        {/* Top "+ New Collection" button (replaces the old top-bar + New) */}
        <Button
          className="w-full mb-3"
          onClick={() => navigate('/app/collections/new')}
        >
          + New Collection
        </Button>

        {isLoading ? (
          <LoadingSpinner fullPage />
        ) : filtered.length === 0 ? (
          <EmptyState
            icon="📋"
            title={collections.length === 0 ? 'No collections yet' : 'Nothing here'}
            description={
              collections.length === 0
                ? 'Create a collection to start splitting expenses'
                : 'No collections match this filter'
            }
          />
        ) : (
          <div className="space-y-3">
            {filtered.map((col) => {
              const role = col.myRole ?? 'MEMBER'
              return (
                <Card
                  key={col.collectionId}
                  onClick={() => navigate(`/app/collections/${col.collectionId}`)}
                  className={(col.status ?? '').toUpperCase() === 'CLOSED' ? 'grayscale opacity-60' : ''}
                >
                  <div className="flex items-center justify-between gap-3">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-1.5 min-w-0">
                        <span className="font-semibold text-gray-900 truncate">{col.name}</span>
                      </div>
                      <p className="flex items-center gap-1.5 text-xs text-gray-400 mt-0.5">
                        <span className="font-semibold uppercase">{(col.typeName ?? col.category)?.toString()}</span>
                        <span className="font-normal uppercase text-gray-400">
                          - {roleLabel[role] ?? 'Member'}
                        </span>
                      </p>
                    </div>
                    <div className="shrink-0 text-right">
                      <p className={`text-sm font-semibold ${
                        (col.myNetBalance ?? 0) > 0 ? 'text-success' : (col.myNetBalance ?? 0) < 0 ? 'text-danger' : 'text-gray-400'
                      }`}>
                        {formatAmount(Math.abs(col.myNetBalance ?? 0), col.currency)}
                      </p>
                      <p className="mt-0.5 text-[10px] uppercase tracking-wide text-gray-400">
                        {(col.myNetBalance ?? 0) > 0 ? 'Receivable' : (col.myNetBalance ?? 0) < 0 ? 'Payable' : 'Settled'}
                      </p>
                    </div>
                  </div>
                </Card>
              )
            })}
          </div>
        )}
      </div>

      <Modal open={showNewCategory} onClose={() => setShowNewCategory(false)} title="New category">
        <div className="space-y-3">
          <Input
            label="Category name"
            value={newCategoryName}
            onChange={(e) => setNewCategoryName(e.target.value)}
            placeholder="Roommates"
            autoFocus
          />
          {categoryError && <p className="text-sm text-danger">{categoryError}</p>}
          <div className="grid grid-cols-2 gap-2">
            <Button variant="secondary" onClick={() => setShowNewCategory(false)}>Cancel</Button>
            <Button
              loading={typeMut.isPending}
              disabled={!newCategoryName.trim()}
              onClick={() => typeMut.mutate()}
            >
              Add
            </Button>
          </div>
          {(typeData?.data?.length ?? 0) > 0 && (
            <div className="border-t border-gray-100 pt-3">
              <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-gray-400">Your types</p>
              <div className="space-y-2">
                {typeData.data.map((type) => (
                  <div key={type.collectionTypeId} className="flex items-center justify-between rounded-lg bg-gray-50 px-3 py-2">
                    <span className="text-sm text-gray-700">{type.name}</span>
                    <button
                      type="button"
                      onClick={() => deleteTypeMut.mutate(type.collectionTypeId)}
                      className="text-xs font-medium text-danger hover:opacity-80"
                    >
                      Delete
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </Modal>
    </PageLayout>
  )
}

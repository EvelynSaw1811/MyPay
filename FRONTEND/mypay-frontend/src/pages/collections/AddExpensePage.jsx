import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import PageLayout from '../../components/layout/PageLayout'
import ExpenseForm from '../../components/collection/ExpenseForm'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import { getCollection, getMembers } from '../../api/collection'
import { getExpense } from '../../api/expense'

export default function AddExpensePage() {
  const { id, eid } = useParams()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const isEdit = !!eid

  const { data: membersData, isLoading: membersLoading } = useQuery({
    queryKey: ['collection', id, 'members'],
    queryFn: () => getMembers(id),
  })
  const { data: colData, isLoading: collectionLoading } = useQuery({
    queryKey: ['collection', id],
    queryFn: () => getCollection(id),
  })
  const { data: expData, isLoading: expLoading } = useQuery({
    queryKey: ['expense', id, eid],
    queryFn: () => getExpense(id, eid),
    enabled: isEdit,
  })

  const members = membersData?.data ?? []
  const collection = colData?.data
  const existing = expData?.data

  if (membersLoading || collectionLoading || (isEdit && expLoading)) {
    return <PageLayout title={isEdit ? 'Edit Expense' : 'Add Expense'} back><LoadingSpinner fullPage /></PageLayout>
  }

  return (
    <PageLayout title={isEdit ? 'Edit Expense' : 'Add Expense'} back>
      <div className="px-4 py-5">
        <ExpenseForm
          collectionId={id}
          currency={existing?.currency ?? collection?.currency ?? 'MYR'}
          expenseId={isEdit ? eid : undefined}
          existing={existing}
          members={members}
          onSaved={() => {
            qc.invalidateQueries({ queryKey: ['collection', id, 'expenses'] })
            navigate(`/app/collections/${id}`)
          }}
        />
      </div>
    </PageLayout>
  )
}

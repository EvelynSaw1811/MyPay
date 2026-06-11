import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import PageLayout from '../../components/layout/PageLayout'
import Card from '../../components/ui/Card'
import Badge from '../../components/ui/Badge'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import EmptyState from '../../components/ui/EmptyState'
import { formatAmount } from '../../utils/currency'
import { formatDate } from '../../utils/date'
import { getCollectionStatement } from '../../api/reporting'

export default function CollectionReportPage() {
  const { id } = useParams()

  const { data, isLoading } = useQuery({
    queryKey: ['report', 'collection', id],
    queryFn: () => getCollectionStatement(id),
  })

  const report = data?.data

  return (
    <PageLayout title="Collection Statement" back>
      <div className="px-4 py-5 space-y-4">
        {isLoading ? (
          <LoadingSpinner fullPage />
        ) : !report ? (
          <EmptyState icon="📋" title="No report data" />
        ) : (
          <>
            <Card>
              <p className="font-semibold text-gray-900 mb-1">{report.collectionName}</p>
              <p className="text-xs text-gray-400">{report.category}</p>
              <div className="mt-3 grid grid-cols-2 gap-3">
                <div>
                  <p className="text-xs text-gray-400">Total expenses</p>
                  <p className="text-lg font-bold text-gray-900">{formatAmount(report.totalAmount ?? 0, report.currency ?? 'MYR')}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-400">Status</p>
                  <Badge color={report.status === 'ACTIVE' ? 'green' : 'gray'} className="mt-1">{report.status}</Badge>
                </div>
              </div>
            </Card>

            {/* Member balances */}
            {report.memberBalances && report.memberBalances.length > 0 && (
              <Card>
                <p className="text-xs font-medium text-gray-500 mb-3">Member balances</p>
                <div className="space-y-2">
                  {report.memberBalances.map((m) => (
                    <div key={m.userId} className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <div className="w-7 h-7 rounded-full bg-primary/10 flex items-center justify-center text-xs font-bold text-primary">
                          {(m.name ?? m.userId ?? '?')[0].toUpperCase()}
                        </div>
                        <span className="text-sm text-gray-700">{m.name ?? m.userId}</span>
                      </div>
                      <span className={`text-sm font-semibold ${(m.netBalance ?? 0) > 0 ? 'text-success' : (m.netBalance ?? 0) < 0 ? 'text-danger' : 'text-gray-400'}`}>
                        {(m.netBalance ?? 0) > 0 ? '+' : ''}{formatAmount(Math.abs(m.netBalance ?? 0), report.currency ?? 'MYR')}
                      </span>
                    </div>
                  ))}
                </div>
              </Card>
            )}

            {/* Expense list */}
            {report.expenses && report.expenses.length > 0 && (
              <div>
                <p className="text-xs font-medium text-gray-500 mb-2">Expenses</p>
                <div className="space-y-2">
                  {report.expenses.map((exp) => (
                    <div key={exp.expenseId} className="flex items-center justify-between py-2 border-b border-gray-50 last:border-0">
                      <div>
                        <p className="text-sm font-medium text-gray-800">{exp.description}</p>
                        <p className="text-xs text-gray-400">{formatDate(exp.createdAt)}</p>
                      </div>
                      <p className="text-sm font-semibold text-gray-900">{formatAmount(exp.amount, exp.currency)}</p>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </PageLayout>
  )
}

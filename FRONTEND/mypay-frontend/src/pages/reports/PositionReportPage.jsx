import { useQuery } from '@tanstack/react-query'
import PageLayout from '../../components/layout/PageLayout'
import Card from '../../components/ui/Card'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import EmptyState from '../../components/ui/EmptyState'
import { formatAmount } from '../../utils/currency'
import { getPosition } from '../../api/reporting'

export default function PositionReportPage() {
  const { data, isLoading } = useQuery({
    queryKey: ['position'],
    queryFn: getPosition,
  })

  const report = data?.data

  return (
    <PageLayout title="My Position" back>
      <div className="px-4 py-5 space-y-4">
        {isLoading ? (
          <LoadingSpinner fullPage />
        ) : !report ? (
          <EmptyState icon="⚖️" title="No position data" />
        ) : (
          <>
            <div className="grid grid-cols-2 gap-3">
              <Card>
                <p className="text-xs text-gray-400">Owed to me</p>
                <p className="text-xl font-bold text-success mt-1">{formatAmount(report.totalReceivable ?? 0, 'MYR')}</p>
              </Card>
              <Card>
                <p className="text-xs text-gray-400">I owe others</p>
                <p className="text-xl font-bold text-danger mt-1">{formatAmount(report.totalPayable ?? 0, 'MYR')}</p>
              </Card>
            </div>

            {/* Per collection breakdown */}
            {report.byCollection && report.byCollection.length > 0 && (
              <div>
                <p className="text-xs font-medium text-gray-500 mb-2">By collection</p>
                <div className="space-y-2">
                  {report.byCollection.map((col) => (
                    <div key={col.collectionId} className="flex items-center justify-between gap-3 py-2 border-b border-gray-50 last:border-0">
                      <p className="text-sm font-medium text-gray-800 min-w-0 flex-1 truncate">
                        {col.collectionName ?? col.collectionId}
                      </p>
                      <span className={`shrink-0 text-sm font-semibold ${(col.balance ?? 0) > 0 ? 'text-success' : (col.balance ?? 0) < 0 ? 'text-danger' : 'text-gray-400'}`}>
                        {(col.balance ?? 0) > 0 ? '+' : ''}{formatAmount(Math.abs(col.balance ?? 0), col.currency ?? 'MYR')}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Net debts */}
            {report.netDebts && report.netDebts.length > 0 && (
              <div>
                <p className="text-xs font-semibold uppercase tracking-wide text-gray-500 mb-2">
                  Outstanding bilateral debts
                </p>
                <div className="space-y-0 divide-y divide-gray-50">
                  {report.netDebts.map((d, i) => (
                    <div key={i} className="flex items-center justify-between gap-3 py-2.5">
                      {/* Creditor name truncates on the left */}
                      <div className="min-w-0 flex-1">
                        <p className="text-sm font-medium text-gray-800 truncate">
                          {d.creditorName ?? d.creditorId}
                        </p>
                        {d.collectionName && (
                          <p className="text-xs text-gray-400 truncate">{d.collectionName}</p>
                        )}
                      </div>
                      {/* Amount always gets full width on the right */}
                      <span className="shrink-0 text-sm font-semibold text-danger">
                        {formatAmount(d.amount, d.currency ?? 'MYR')}
                      </span>
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

import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import PageLayout from '../../components/layout/PageLayout'
import Card from '../../components/ui/Card'
import Badge from '../../components/ui/Badge'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import EmptyState from '../../components/ui/EmptyState'
import { formatAmount } from '../../utils/currency'
import { formatDate } from '../../utils/date'
import { getLedger } from '../../api/reporting'

export default function LedgerReportPage() {
  const { currency } = useParams()

  const { data, isLoading } = useQuery({
    queryKey: ['report', 'ledger', currency],
    queryFn: () => getLedger(currency),
  })

  const report = data?.data

  return (
    <PageLayout title={`${currency} Ledger`} back>
      <div className="px-4 py-5 space-y-4">
        {isLoading ? (
          <LoadingSpinner fullPage />
        ) : !report ? (
          <EmptyState icon="📒" title="No ledger data" />
        ) : (
          <>
            <div className="grid grid-cols-3 gap-2">
              <Card>
                <p className="text-xs text-gray-400">Inflow</p>
                <p className="text-sm font-bold text-success mt-1">{formatAmount(report.totalInflow ?? 0, currency)}</p>
              </Card>
              <Card>
                <p className="text-xs text-gray-400">Outflow</p>
                <p className="text-sm font-bold text-danger mt-1">{formatAmount(report.totalOutflow ?? 0, currency)}</p>
              </Card>
              <Card>
                <p className="text-xs text-gray-400">Net</p>
                <p className={`text-sm font-bold mt-1 ${(report.net ?? 0) > 0 ? 'text-success' : (report.net ?? 0) < 0 ? 'text-danger' : 'text-gray-400'}`}>
                  {formatAmount(Math.abs(report.net ?? 0), currency)}
                </p>
              </Card>
            </div>

            {report.entries && report.entries.length > 0 ? (
              <div className="space-y-1">
                {report.entries.map((e, i) => (
                  <div key={i} className="flex items-center gap-3 py-2.5 border-b border-gray-50 last:border-0">
                    <div className="flex-1 min-w-0">
                      <p className="text-sm text-gray-800 truncate">{e.description ?? e.type}</p>
                      <p className="text-xs text-gray-400">{formatDate(e.date ?? e.createdAt)}</p>
                    </div>
                    <div className="text-right">
                      <p className={`text-sm font-semibold ${e.direction === 'IN' ? 'text-success' : 'text-danger'}`}>
                        {e.direction === 'IN' ? '+' : '-'}{formatAmount(e.amount, currency)}
                      </p>
                      <Badge color={e.direction === 'IN' ? 'green' : 'red'}>{e.direction}</Badge>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <EmptyState icon="📭" title="No transactions in this currency" />
            )}
          </>
        )}
      </div>
    </PageLayout>
  )
}

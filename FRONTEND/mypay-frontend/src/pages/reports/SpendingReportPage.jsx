import { useQuery } from '@tanstack/react-query'
import PageLayout from '../../components/layout/PageLayout'
import Card from '../../components/ui/Card'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import { formatAmount } from '../../utils/currency'
import { getSpending } from '../../api/reporting'

export default function SpendingReportPage() {
  const { data, isLoading } = useQuery({
    queryKey: ['report', 'spending'],
    queryFn: getSpending,
  })

  const report = data?.data

  return (
    <PageLayout title="Spending Report" back>
      <div className="px-4 py-5 space-y-4">
        {isLoading ? (
          <LoadingSpinner fullPage />
        ) : !report ? (
          <p className="text-sm text-gray-400 text-center py-10">No data available</p>
        ) : (
          <>
            <div className="grid grid-cols-2 gap-3">
              <Card>
                <p className="text-xs text-gray-400">Total spent</p>
                <p className="text-lg font-bold text-danger mt-1">{formatAmount(report.totalSpent ?? 0, 'MYR')}</p>
              </Card>
              <Card>
                <p className="text-xs text-gray-400">Total received</p>
                <p className="text-lg font-bold text-success mt-1">{formatAmount(report.totalReceived ?? 0, 'MYR')}</p>
              </Card>
            </div>

            {report.byCategory && Object.entries(report.byCategory).length > 0 && (
              <Card>
                <p className="text-xs font-medium text-gray-500 mb-3">Breakdown by category</p>
                <div className="space-y-2">
                  {Object.entries(report.byCategory).map(([cat, amt]) => (
                    <div key={cat} className="flex justify-between items-center">
                      <span className="text-sm text-gray-700">{cat}</span>
                      <span className="text-sm font-semibold text-gray-900">{formatAmount(amt, 'MYR')}</span>
                    </div>
                  ))}
                </div>
              </Card>
            )}

            {report.byCurrency && Object.entries(report.byCurrency).length > 0 && (
              <Card>
                <p className="text-xs font-medium text-gray-500 mb-3">Breakdown by currency</p>
                <div className="space-y-2">
                  {Object.entries(report.byCurrency).map(([cur, amt]) => (
                    <div key={cur} className="flex justify-between items-center">
                      <span className="text-sm text-gray-700">{cur}</span>
                      <span className="text-sm font-semibold text-gray-900">{formatAmount(amt, cur)}</span>
                    </div>
                  ))}
                </div>
              </Card>
            )}
          </>
        )}
      </div>
    </PageLayout>
  )
}

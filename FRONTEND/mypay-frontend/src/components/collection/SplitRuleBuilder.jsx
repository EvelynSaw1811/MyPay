import { useState } from 'react'
import Select from '../ui/Select'
import Input from '../ui/Input'

const STRATEGIES = [
  { value: 'EQUAL',         label: 'Equal — split evenly' },
  { value: 'PERCENTAGE',    label: 'Percentage — custom %' },
  { value: 'EXACT',         label: 'Exact — fixed amounts' },
  { value: 'HYBRID',        label: 'Hybrid — fixed + split remainder' },
  { value: 'HIERARCHICAL',  label: 'Hierarchical — primary / secondary' },
]

const TAX_TYPES = [
  { value: '', label: 'No tax' },
  { value: 'SST', label: 'SST (Sales & Services Tax)' },
  { value: 'SERVICE_CHARGE', label: 'Service Charge' },
  { value: 'OTHER', label: 'Other' },
]

function buildDefaultOverrides(members, strategy) {
  return members.map((m) => ({
    userId: m.userId ?? m.id,
    name: m.name ?? m.userId ?? m.id,
    percentage: strategy === 'PERCENTAGE' ? Math.floor(100 / members.length) : undefined,
    amount: strategy === 'EXACT' || strategy === 'HYBRID' ? 0 : undefined,
    tier: strategy === 'HIERARCHICAL' ? 'PRIMARY' : undefined,
  }))
}

export default function SplitRuleBuilder({ members = [], value, onChange }) {
  const [type, setType] = useState(value?.type ?? 'EQUAL')
  const [taxType, setTaxType] = useState(value?.taxType ?? '')
  const [taxRate, setTaxRate] = useState(value?.taxRate ?? 0)
  const [overrides, setOverrides] = useState(() => {
    if (value?.overrides?.length) return value.overrides
    return buildDefaultOverrides(members, value?.type ?? 'EQUAL')
  })

  // Reset overrides when the split strategy changes (handled inline in handleTypeChange)

  function emit(patch) {
    const next = { type, taxType: taxType || undefined, taxRate: taxRate || undefined, overrides, ...patch }
    onChange?.(next)
  }

  function handleTypeChange(newType) {
    setType(newType)
    const newOverrides = buildDefaultOverrides(members, newType)
    setOverrides(newOverrides)
    emit({ type: newType, overrides: newOverrides })
  }

  function handleTaxTypeChange(v) {
    setTaxType(v)
    if (!v) setTaxRate(0)
    emit({ taxType: v || undefined, taxRate: v ? taxRate : undefined })
  }

  function handleTaxRateChange(v) {
    const n = parseFloat(v) || 0
    setTaxRate(n)
    emit({ taxRate: n || undefined })
  }

  function updateOverride(index, field, rawValue) {
    const updated = overrides.map((o, i) => {
      if (i !== index) return o
      const value = field === 'percentage' || field === 'amount' ? parseFloat(rawValue) || 0 : rawValue
      return { ...o, [field]: value }
    })
    setOverrides(updated)
    emit({ overrides: updated })
  }

  const percentTotal = overrides.reduce((s, o) => s + (o.percentage ?? 0), 0)
  const percentError = type === 'PERCENTAGE' && Math.abs(percentTotal - 100) > 0.01
    ? `Total is ${percentTotal.toFixed(1)}% (must equal 100%)`
    : null

  return (
    <div className="space-y-4">
      <Select
        label="Split strategy"
        value={type}
        onChange={(e) => handleTypeChange(e.target.value)}
        options={STRATEGIES}
      />

      {/* Tax section */}
      <div className="grid grid-cols-2 gap-3">
        <Select
          label="Tax type"
          value={taxType}
          onChange={(e) => handleTaxTypeChange(e.target.value)}
          options={TAX_TYPES}
        />
        {taxType && (
          <Input
            label="Tax rate (%)"
            type="number"
            min="0"
            max="100"
            step="0.1"
            value={taxRate}
            onChange={(e) => handleTaxRateChange(e.target.value)}
          />
        )}
      </div>

      {/* EQUAL: nothing extra to configure */}

      {/* PERCENTAGE */}
      {type === 'PERCENTAGE' && (
        <div className="space-y-2">
          <p className="text-xs font-medium text-gray-500">Assign percentages (must total 100%)</p>
          {overrides.map((o, i) => (
            <div key={o.userId} className="flex items-center gap-3">
              <span className="text-sm text-gray-700 flex-1 truncate">{o.name}</span>
              <div className="w-28">
                <Input
                  type="number"
                  min="0"
                  max="100"
                  step="0.1"
                  value={o.percentage ?? 0}
                  onChange={(e) => updateOverride(i, 'percentage', e.target.value)}
                  suffix="%"
                />
              </div>
            </div>
          ))}
          {percentError && <p className="text-xs text-danger">{percentError}</p>}
        </div>
      )}

      {/* EXACT */}
      {type === 'EXACT' && (
        <div className="space-y-2">
          <p className="text-xs font-medium text-gray-500">Assign exact amounts</p>
          {overrides.map((o, i) => (
            <div key={o.userId} className="flex items-center gap-3">
              <span className="text-sm text-gray-700 flex-1 truncate">{o.name}</span>
              <div className="w-32">
                <Input
                  type="number"
                  min="0"
                  step="0.01"
                  value={o.amount ?? 0}
                  onChange={(e) => updateOverride(i, 'amount', e.target.value)}
                />
              </div>
            </div>
          ))}
        </div>
      )}

      {/* HYBRID */}
      {type === 'HYBRID' && (
        <div className="space-y-2">
          <p className="text-xs font-medium text-gray-500">
            Fixed amounts per member — remainder is split equally
          </p>
          {overrides.map((o, i) => (
            <div key={o.userId} className="flex items-center gap-3">
              <span className="text-sm text-gray-700 flex-1 truncate">{o.name}</span>
              <div className="w-32">
                <Input
                  type="number"
                  min="0"
                  step="0.01"
                  value={o.amount ?? 0}
                  onChange={(e) => updateOverride(i, 'amount', e.target.value)}
                />
              </div>
            </div>
          ))}
        </div>
      )}

      {/* HIERARCHICAL */}
      {type === 'HIERARCHICAL' && (
        <div className="space-y-2">
          <p className="text-xs font-medium text-gray-500">
            PRIMARY members share first; SECONDARY members share remainder
          </p>
          {overrides.map((o, i) => (
            <div key={o.userId} className="flex items-center gap-3">
              <span className="text-sm text-gray-700 flex-1 truncate">{o.name}</span>
              <div className="w-36">
                <Select
                  value={o.tier ?? 'PRIMARY'}
                  onChange={(e) => updateOverride(i, 'tier', e.target.value)}
                  options={[
                    { value: 'PRIMARY', label: 'Primary' },
                    { value: 'SECONDARY', label: 'Secondary' },
                  ]}
                />
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

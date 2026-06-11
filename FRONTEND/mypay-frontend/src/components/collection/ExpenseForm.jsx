import { useState, useMemo } from 'react'
import Button from '../ui/Button'
import { createExpense, updateExpense } from '../../api/expense'
import { formatAmount } from '../../utils/currency'
import { getApiErrorMessage } from '../../utils/apiError'

/**
 * ExpenseForm — used inside the "New expense" modal on CollectionDetailPage.
 *
 * Props:
 *   collectionId  string   required
 *   currency      string   required — collection currency (no selector shown)
 *   members       array    required — collection members
 *   existing      object   optional — pre-fills when editing
 *   expenseId     string   optional — required when editing
 *   onSaved       fn
 *   onCancel      fn
 *   showCancel    bool
 *   submitLabel   string
 */

// ─── Constants ───────────────────────────────────────────────────────────────

/**
 * Preset split strategies.
 * All presets divide the TOTAL expense amount equally among involved members.
 * The labels are informational — they describe what is already included in the total.
 */
const PRESET_STRATEGIES = [
  {
    value: 'PRESET_EQUAL',
    label: 'Equal division',
    hint: 'Total divided equally among selected members.',
  },
  {
    value: 'PRESET_ST_SST_SEQ',
    label: 'Service Tax 10%  →  SST 6%',
    hint: 'Total already includes 10% service tax, then 6% SST on subtotal (×1.166 of net).',
  },
  {
    value: 'PRESET_ST_SST_COMB',
    label: 'Service Tax 10%  +  SST 6%',
    hint: 'Total already includes both 10% service tax and 6% SST on base (×1.16 of net).',
  },
  {
    value: 'PRESET_SST',
    label: 'SST 6%',
    hint: 'Total already includes 6% SST (×1.06 of net).',
  },
  {
    value: 'PRESET_CUSTOM_PCT',
    label: 'Custom %',
    hint: 'Total already includes a custom percentage charge on top of net.',
  },
  {
    value: 'CUSTOM',
    label: 'Custom (multi-layer)',
    hint: 'Define up to 6 independent layers. All layer totals must sum to the expense total.',
  },
]

/** Types available per CUSTOM layer */
const LAYER_TYPES = [
  { value: 'EQUAL_TOTAL', label: 'Equal division (total)' },
  { value: 'EQUAL_EACH',  label: 'Equal division (each)' },
  { value: 'PERCENTAGE',  label: 'Percentage' },
  { value: 'FIXED',       label: 'Fixed amount' },
  { value: 'FIX_TAX',    label: 'Fix tax' },
]

// ─── Helpers ─────────────────────────────────────────────────────────────────

function round2(n) { return Math.round(n * 100) / 100 }

/** Compute a layer's total contribution amount */
function layerTotal(layer, expenseTotal) {
  switch (layer.type) {
    case 'EQUAL_TOTAL': return Number(layer.amount) || 0
    case 'EQUAL_EACH':  return (Number(layer.amount) || 0) * layer.members.length
    case 'PERCENTAGE':  return Number(layer.amount) || 0
    case 'FIXED':       return (layer.entries ?? []).reduce((s, e) => s + (Number(e.amount) || 0), 0)
    case 'FIX_TAX':     return round2((expenseTotal * (Number(layer.taxPct) || 0)) / 100)
    default:            return 0
  }
}

/** Compute per-member amounts for one layer */
function layerPerMember(layer, expenseTotal) {
  const result = {}
  const total = layerTotal(layer, expenseTotal)

  switch (layer.type) {
    case 'EQUAL_TOTAL':
    case 'FIX_TAX': {
      const n = layer.members.length
      if (n === 0 || total === 0) break
      const per = round2(total / n)
      for (const uid of layer.members) result[uid] = (result[uid] ?? 0) + per
      break
    }
    case 'EQUAL_EACH': {
      const per = Number(layer.amount) || 0
      for (const uid of layer.members) result[uid] = (result[uid] ?? 0) + per
      break
    }
    case 'PERCENTAGE': {
      for (const uid of layer.members) {
        const share = round2(total * (layer.percentages?.[uid] ?? 0) / 100)
        result[uid] = (result[uid] ?? 0) + share
      }
      break
    }
    case 'FIXED': {
      for (const entry of layer.entries ?? []) {
        if (entry.userId) {
          result[entry.userId] = (result[entry.userId] ?? 0) + (Number(entry.amount) || 0)
        }
      }
      break
    }
  }
  return result
}

function initLayer(involvedIds) {
  return {
    type: 'EQUAL_TOTAL',
    amount: '',
    taxPct: 10,
    members: [...involvedIds],
    percentages: {},
    entries: [{ userId: involvedIds[0] ?? '', amount: '' }],
  }
}

// ─── Component ───────────────────────────────────────────────────────────────

export default function ExpenseForm({
  collectionId,
  currency = 'MYR',
  members = [],
  existing,
  expenseId,
  onSaved,
  onCancel,
  showCancel,
  submitLabel,
}) {
  const isEdit = !!expenseId

  const memberByUserId = useMemo(
    () => Object.fromEntries(members.map((m) => [m.userId, m])),
    [members],
  )

  // ── Basic fields ──────────────────────────────────────────────────────────
  const [title, setTitle]           = useState(existing?.title ?? '')
  const [description, setDescription] = useState(existing?.description ?? '')
  const [amount, setAmount]         = useState(existing?.amount ?? '')
  const [paidBy, setPaidBy]         = useState(
    existing?.paidBy ?? members[0]?.userId ?? '',
  )

  // ── Involved members ──────────────────────────────────────────────────────
  const [involvedIds, setInvolvedIds] = useState(() => members.map((m) => m.userId))

  // ── Strategy ──────────────────────────────────────────────────────────────
  const [strategy, setStrategy]     = useState(() => {
    if (!existing?.splitType) return 'PRESET_EQUAL'
    return existing.splitType === 'EQUAL' ? 'PRESET_EQUAL' : 'CUSTOM'
  })
  const [customPct, setCustomPct]   = useState(10)
  const [numLayers, setNumLayers]   = useState(2)
  const [numLayersRaw, setNumLayersRaw] = useState('2') // tracks raw input for red-label validation

  // ── CUSTOM layers ─────────────────────────────────────────────────────────
  const [layers, setLayers] = useState(() =>
    [initLayer(members.map((m) => m.userId)), initLayer(members.map((m) => m.userId))]
  )

  const [error, setError]     = useState('')
  const [loading, setLoading] = useState(false)

  // ── Member toggles ────────────────────────────────────────────────────────

  function toggleMember(userId) {
    setInvolvedIds((prev) => {
      const next = prev.includes(userId)
        ? prev.filter((id) => id !== userId)
        : [...prev, userId]
      setLayers((ls) =>
        ls.map((l) => ({ ...l, members: l.members.filter((id) => next.includes(id)) }))
      )
      return next
    })
  }

  function selectAllMembers() {
    const all = members.map((m) => m.userId)
    setInvolvedIds(all)
    setLayers((ls) => ls.map((l) => ({ ...l, members: all })))
  }

  function deselectAllMembers() {
    setInvolvedIds([])
    setLayers((ls) => ls.map((l) => ({ ...l, members: [] })))
  }

  // ── Layer management ──────────────────────────────────────────────────────

  function handleNumLayersChange(raw) {
    setNumLayersRaw(raw)
    const n = parseInt(raw)
    if (n >= 1 && n <= 6) {
      setNumLayers(n)
      setLayers((prev) => {
        const next = [...prev]
        while (next.length < n) next.push(initLayer(involvedIds))
        return next.slice(0, n)
      })
    }
  }

  function updateLayer(li, patch) {
    setLayers((prev) => prev.map((l, i) => (i === li ? { ...l, ...patch } : l)))
  }

  function toggleLayerMember(li, uid) {
    setLayers((prev) =>
      prev.map((l, i) => {
        if (i !== li) return l
        const next = l.members.includes(uid)
          ? l.members.filter((id) => id !== uid)
          : [...l.members, uid]
        return { ...l, members: next }
      })
    )
  }

  function updateLayerPct(li, uid, pct) {
    setLayers((prev) =>
      prev.map((l, i) =>
        i === li ? { ...l, percentages: { ...l.percentages, [uid]: Number(pct) || 0 } } : l
      )
    )
  }

  function addLayerEntry(li) {
    setLayers((prev) =>
      prev.map((l, i) =>
        i === li
          ? { ...l, entries: [...l.entries, { userId: involvedIds[0] ?? '', amount: '' }] }
          : l
      )
    )
  }

  function updateLayerEntry(li, ei, field, value) {
    setLayers((prev) =>
      prev.map((l, i) => {
        if (i !== li) return l
        return { ...l, entries: l.entries.map((e, j) => (j === ei ? { ...e, [field]: value } : e)) }
      })
    )
  }

  function removeLayerEntry(li, ei) {
    setLayers((prev) =>
      prev.map((l, i) =>
        i === li ? { ...l, entries: l.entries.filter((_, j) => j !== ei) } : l
      )
    )
  }

  // ── Computed summary ──────────────────────────────────────────────────────

  const expenseTotal = Number(amount) || 0

  const summary = useMemo(() => {
    if (strategy !== 'CUSTOM') {
      const n = involvedIds.length
      if (n === 0 || expenseTotal === 0) return { perMember: {}, total: expenseTotal }
      const per = round2(expenseTotal / n)
      return {
        perMember: Object.fromEntries(involvedIds.map((uid) => [uid, per])),
        total: expenseTotal,
      }
    }

    // CUSTOM: accumulate per-member from each layer
    const perMember = {}
    for (const layer of layers) {
      const lpm = layerPerMember(layer, expenseTotal)
      for (const [uid, amt] of Object.entries(lpm)) {
        perMember[uid] = round2((perMember[uid] ?? 0) + amt)
      }
    }

    const computedTotal = Object.values(perMember).reduce((s, v) => round2(s + v), 0)
    return { perMember, total: computedTotal }
  }, [strategy, expenseTotal, involvedIds, layers])

  /** Running cumulative total after layer `li` (inclusive) */
  function cumulativeAfter(li) {
    let sum = 0
    for (let i = 0; i <= li; i++) sum += layerTotal(layers[i], expenseTotal)
    return round2(sum)
  }

  // ── Validation helpers ────────────────────────────────────────────────────

  function pctLayerError(layer) {
    if (layer.type !== 'PERCENTAGE' || layer.members.length === 0) return null
    const total = layer.members.reduce((s, uid) => s + (layer.percentages?.[uid] ?? 0), 0)
    return Math.abs(total - 100) > 0.1
      ? `Percentages sum to ${total.toFixed(1)}% — must equal 100%.`
      : null
  }

  const numLayersInvalid = (() => {
    const n = parseInt(numLayersRaw)
    return isNaN(n) || n < 1 || n > 6
  })()

  const customSumMismatch =
    strategy === 'CUSTOM' &&
    expenseTotal > 0 &&
    Math.abs(summary.total - expenseTotal) > 0.02

  // ── Submit ────────────────────────────────────────────────────────────────

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')

    if (!title.trim()) { setError('Title is required.'); return }
    if (title.trim().length > 30) { setError('Title must be at most 30 characters.'); return }
    if (description.length > 100) { setError('Description must be at most 100 characters.'); return }
    if (involvedIds.length === 0) { setError('Select at least one involved member.'); return }
    if (expenseTotal <= 0) { setError('Amount must be greater than 0.'); return }
    if (customSumMismatch) {
      setError(
        `Layer totals sum to ${formatAmount(summary.total, currency)} but the expense total is ${formatAmount(expenseTotal, currency)}. Please balance the layers.`
      )
      return
    }

    setLoading(true)
    try {
      let payload

      if (strategy !== 'CUSTOM') {
        payload = {
          title: title.trim(),
          description: description.trim() || null,
          amount: expenseTotal,
          currency,
          paidBy,
          splitType: 'EQUAL',
          participants: involvedIds.map((uid) => ({ userId: uid })),
        }
      } else {
        // Flatten CUSTOM layers to per-member EXACT amounts
        const perMemberFinal = {}
        for (const layer of layers) {
          const lpm = layerPerMember(layer, expenseTotal)
          for (const [uid, amt] of Object.entries(lpm)) {
            perMemberFinal[uid] = round2((perMemberFinal[uid] ?? 0) + amt)
          }
        }
        payload = {
          title: title.trim(),
          description: description.trim() || null,
          amount: expenseTotal,
          currency,
          paidBy,
          splitType: 'EXACT',
          participants: Object.entries(perMemberFinal).map(([userId, fixedAmount]) => ({
            userId,
            fixedAmount,
          })),
        }
      }

      if (isEdit) {
        await updateExpense(collectionId, expenseId, payload)
      } else {
        await createExpense(collectionId, payload)
      }
      onSaved?.()
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to save expense'))
    } finally {
      setLoading(false)
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Render helpers
  // ─────────────────────────────────────────────────────────────────────────

  const fieldClass =
    'w-full rounded-lg border border-[#E8E8E8] px-3 py-2 text-sm outline-none focus:border-primary bg-white'

  const showSummary = involvedIds.length > 0 && expenseTotal > 0

  return (
    <form onSubmit={handleSubmit} className="space-y-5">

      {/* ── Title ────────────────────────────────────────────────────────── */}
      <div>
        <div className="flex items-center justify-between mb-1">
          <label className="text-sm font-medium text-gray-700">Title</label>
          <span className={`text-xs ${title.length > 30 ? 'text-danger' : 'text-gray-400'}`}>
            {title.length}/30
          </span>
        </div>
        <input
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="e.g. Dinner at Restoran ABC"
          maxLength={30}
          required
          className={fieldClass}
        />
      </div>

      {/* ── Description (optional) ───────────────────────────────────────── */}
      <div>
        <div className="flex items-center justify-between mb-1">
          <label className="text-sm font-medium text-gray-700">
            Description
            <span className="ml-1 text-xs font-normal text-gray-400">(optional)</span>
          </label>
          <span className={`text-xs ${description.length > 100 ? 'text-danger' : 'text-gray-400'}`}>
            {description.length}/100
          </span>
        </div>
        <textarea
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          placeholder="Additional notes about this expense…"
          maxLength={100}
          rows={2}
          className={`${fieldClass} resize-none`}
        />
      </div>

      {/* ── Total amount ─────────────────────────────────────────────────── */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">
          Total amount
          <span className="ml-1.5 text-xs font-normal text-gray-400">({currency})</span>
        </label>
        <input
          type="number"
          min="0.01"
          step="0.01"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
          placeholder="0.00"
          required
          className={fieldClass}
        />
      </div>

      {/* ── Payer ────────────────────────────────────────────────────────── */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Paid by</label>
        <select
          value={paidBy}
          onChange={(e) => setPaidBy(e.target.value)}
          className={fieldClass}
        >
          {members.map((m) => (
            <option key={m.userId} value={m.userId}>
              {m.userNickname ?? m.name ?? m.userId}
              {m.userId === members[0]?.userId ? ' (you)' : ''}
            </option>
          ))}
        </select>
      </div>

      {/* ── Involved members ─────────────────────────────────────────────── */}
      <div>
        <div className="flex items-center justify-between mb-2">
          <p className="text-sm font-medium text-gray-700">
            Involved members
            <span className="ml-1.5 text-xs font-normal text-gray-400">
              ({involvedIds.length} selected)
            </span>
          </p>
          <div className="flex gap-3">
            <button type="button" onClick={selectAllMembers}
              className="text-xs font-medium text-primary hover:underline">All</button>
            <button type="button" onClick={deselectAllMembers}
              className="text-xs font-medium text-gray-400 hover:underline">None</button>
          </div>
        </div>
        {/* max 10 rows visible; scrollable if more */}
        <div className="rounded-lg border border-[#E8E8E8] divide-y divide-gray-50 overflow-y-auto"
          style={{ maxHeight: '260px' }}>
          {members.map((m) => {
            const nickname = m.userNickname ?? m.name ?? m.userId
            const checked  = involvedIds.includes(m.userId)
            return (
              <label
                key={m.userId}
                className="flex items-center gap-2.5 px-3 py-2 cursor-pointer hover:bg-gray-50 select-none"
              >
                <input
                  type="checkbox"
                  checked={checked}
                  onChange={() => toggleMember(m.userId)}
                  className="h-4 w-4 accent-primary rounded shrink-0"
                />
                <span className="text-sm text-gray-800 flex-1 truncate">{nickname}</span>
                <span className="text-[10px] text-gray-400 shrink-0">{m.role}</span>
              </label>
            )
          })}
        </div>
        {involvedIds.length === 0 && (
          <p className="text-xs text-danger mt-1">Select at least one member.</p>
        )}
      </div>

      {/* ── Split strategy ───────────────────────────────────────────────── */}
      <div>
        <p className="text-sm font-medium text-gray-700 mb-2">Split strategy</p>
        <div className="space-y-1.5">
          {PRESET_STRATEGIES.map((s) => {
            const active = strategy === s.value
            return (
              <label
                key={s.value}
                className={`flex items-center gap-3 rounded-lg border px-3 py-2.5 cursor-pointer transition-colors select-none ${
                  active ? 'border-primary bg-blue-50' : 'border-[#E8E8E8] hover:border-gray-300'
                }`}
              >
                <input
                  type="radio"
                  name="strategy"
                  value={s.value}
                  checked={active}
                  onChange={() => setStrategy(s.value)}
                  className="accent-primary shrink-0"
                />
                <span className="text-sm flex-1">{s.label}</span>

                {/* Inline custom % field */}
                {s.value === 'PRESET_CUSTOM_PCT' && active && (
                  <div className="flex items-center gap-1 shrink-0" onClick={(e) => e.stopPropagation()}>
                    <input
                      type="number" min="0" max="999" step="0.1"
                      value={customPct}
                      onChange={(e) => setCustomPct(e.target.value)}
                      className="w-16 rounded border border-gray-300 px-2 py-0.5 text-sm text-right outline-none focus:border-primary"
                    />
                    <span className="text-sm text-gray-500">%</span>
                  </div>
                )}

                {/* Inline layer count field */}
                {s.value === 'CUSTOM' && active && (
                  <div className="flex items-center gap-1.5 shrink-0" onClick={(e) => e.stopPropagation()}>
                    <input
                      type="number" min="1" max="6" step="1"
                      value={numLayersRaw}
                      onChange={(e) => handleNumLayersChange(e.target.value)}
                      className={`w-12 rounded border px-2 py-0.5 text-sm text-center outline-none focus:border-primary ${
                        numLayersInvalid ? 'border-red-400 text-red-500' : 'border-gray-300'
                      }`}
                    />
                    <span className={`text-xs shrink-0 ${numLayersInvalid ? 'text-red-500' : 'text-gray-500'}`}>
                      {numLayersInvalid ? 'Enter 1–6' : 'layers'}
                    </span>
                  </div>
                )}
              </label>
            )
          })}
        </div>
        {strategy && (
          <p className="mt-1.5 text-[11px] text-gray-400 leading-relaxed pl-0.5">
            {PRESET_STRATEGIES.find((s) => s.value === strategy)?.hint}
          </p>
        )}
      </div>

      {/* ── CUSTOM layer builders ─────────────────────────────────────────── */}
      {strategy === 'CUSTOM' && !numLayersInvalid && (
        <div className="space-y-3">
          {layers.map((layer, li) => {
            const lTotal     = layerTotal(layer, expenseTotal)
            const cumulative = cumulativeAfter(li)
            const remaining  = round2(expenseTotal - cumulative)
            const pctErr     = pctLayerError(layer)

            return (
              <div key={li} className="rounded-xl border border-gray-200 bg-gray-50/40 p-3 space-y-3">

                {/* Layer header */}
                <div className="flex items-center gap-2">
                  <span className="text-xs font-semibold text-gray-400 uppercase tracking-wide w-16 shrink-0">
                    Layer {li + 1}
                  </span>

                  {/* Type selector */}
                  <select
                    value={layer.type}
                    onChange={(e) => updateLayer(li, { type: e.target.value })}
                    className="flex-1 rounded-lg border border-[#E8E8E8] px-2 py-1.5 text-sm outline-none focus:border-primary bg-white"
                  >
                    {LAYER_TYPES.map((t) => (
                      <option key={t.value} value={t.value}>{t.label}</option>
                    ))}
                  </select>

                  {/* Amount / tax input (right of type) */}
                  {layer.type === 'FIX_TAX' ? (
                    <div className="flex items-center gap-1 shrink-0">
                      <input
                        type="number" min="0" max="100" step="0.1"
                        value={layer.taxPct}
                        onChange={(e) => updateLayer(li, { taxPct: e.target.value })}
                        className="w-16 rounded-lg border border-[#E8E8E8] px-2 py-1.5 text-sm text-right outline-none focus:border-primary bg-white"
                      />
                      <span className="text-xs text-gray-400">%</span>
                    </div>
                  ) : layer.type === 'FIXED' ? (
                    <span className="text-xs text-gray-400 shrink-0 w-24 text-right">
                      Auto
                    </span>
                  ) : (
                    <div className="flex items-center gap-1 shrink-0">
                      <input
                        type="number" min="0" step="0.01"
                        value={layer.amount}
                        onChange={(e) => updateLayer(li, { amount: e.target.value })}
                        placeholder="0.00"
                        className="w-24 rounded-lg border border-[#E8E8E8] px-2 py-1.5 text-sm text-right outline-none focus:border-primary bg-white"
                      />
                      <span className="text-xs text-gray-400">{currency}</span>
                    </div>
                  )}
                </div>

                {/* FIX_TAX hint */}
                {layer.type === 'FIX_TAX' && (
                  <p className="text-[11px] text-gray-400 -mt-1">
                    Tax = {formatAmount(lTotal, currency)} ({layer.taxPct || 0}% of expense total {formatAmount(expenseTotal, currency)})
                  </p>
                )}

                {/* EQUAL_EACH hint */}
                {layer.type === 'EQUAL_EACH' && (
                  <p className="text-[11px] text-gray-400 -mt-1">
                    Each selected member pays {formatAmount(Number(layer.amount) || 0, currency)} individually.
                  </p>
                )}

                {/* Member section — EQUAL_TOTAL, EQUAL_EACH, PERCENTAGE, FIX_TAX */}
                {layer.type !== 'FIXED' && (
                  <div>
                    <p className="text-xs text-gray-500 mb-1.5">Involved in this layer</p>
                    {involvedIds.length === 0 ? (
                      <p className="text-xs text-gray-400 italic">No members selected above.</p>
                    ) : (
                      <div className="space-y-1">
                        {involvedIds.map((uid) => {
                          const nickname = memberByUserId[uid]?.userNickname ?? memberByUserId[uid]?.name ?? uid
                          const checked  = layer.members.includes(uid)
                          return (
                            <label key={uid} className="flex items-center gap-2.5 cursor-pointer py-0.5 select-none">
                              <input
                                type="checkbox"
                                checked={checked}
                                onChange={() => toggleLayerMember(li, uid)}
                                className="h-3.5 w-3.5 accent-primary rounded shrink-0"
                              />
                              <span className="text-sm text-gray-700 flex-1">{nickname}</span>
                              {layer.type === 'PERCENTAGE' && checked && (
                                <div className="flex items-center gap-1 shrink-0">
                                  <input
                                    type="number" min="0" max="100" step="0.1"
                                    value={layer.percentages?.[uid] ?? ''}
                                    onChange={(e) => updateLayerPct(li, uid, e.target.value)}
                                    placeholder="0"
                                    className="w-16 rounded border border-gray-300 px-2 py-0.5 text-sm text-right outline-none focus:border-primary"
                                  />
                                  <span className="text-xs text-gray-400">%</span>
                                </div>
                              )}
                            </label>
                          )
                        })}
                      </div>
                    )}
                    {pctErr && <p className="text-xs text-danger mt-1.5">{pctErr}</p>}
                  </div>
                )}

                {/* FIXED: per-entry rows */}
                {layer.type === 'FIXED' && (
                  <div>
                    <p className="text-xs text-gray-500 mb-1.5">Amounts per member</p>
                    <div className="space-y-1.5">
                      {(layer.entries ?? []).map((entry, ei) => (
                        <div key={ei} className="flex items-center gap-2">
                          <select
                            value={entry.userId}
                            onChange={(e) => updateLayerEntry(li, ei, 'userId', e.target.value)}
                            className="flex-1 rounded-lg border border-[#E8E8E8] px-2 py-1.5 text-sm outline-none focus:border-primary bg-white"
                          >
                            <option value="">Select member</option>
                            {involvedIds.map((uid) => (
                              <option key={uid} value={uid}>
                                {memberByUserId[uid]?.userNickname ?? memberByUserId[uid]?.name ?? uid}
                              </option>
                            ))}
                          </select>
                          <input
                            type="number" min="0" step="0.01"
                            value={entry.amount}
                            onChange={(e) => updateLayerEntry(li, ei, 'amount', e.target.value)}
                            placeholder="0.00"
                            className="w-24 rounded-lg border border-[#E8E8E8] px-2 py-1.5 text-sm text-right outline-none focus:border-primary bg-white"
                          />
                          {layer.entries.length > 1 && (
                            <button
                              type="button"
                              onClick={() => removeLayerEntry(li, ei)}
                              className="shrink-0 text-gray-300 hover:text-danger text-xl leading-none"
                            >×</button>
                          )}
                        </div>
                      ))}
                    </div>
                    <button
                      type="button"
                      onClick={() => addLayerEntry(li)}
                      className="mt-2 text-xs text-primary font-medium hover:underline"
                    >
                      + Add entry
                    </button>
                  </div>
                )}

                {/* Layer total + remaining — bottom right */}
                <div className="flex items-center justify-end gap-3 pt-1 border-t border-gray-100">
                  <span className="text-xs text-gray-400">
                    Layer total:
                    <span className="ml-1 font-medium text-gray-700">{formatAmount(lTotal, currency)}</span>
                  </span>
                  <span className={`text-xs font-medium ${remaining < -0.01 ? 'text-danger' : remaining < 0.01 ? 'text-success' : 'text-gray-500'}`}>
                    Remaining: {formatAmount(remaining, currency)}
                  </span>
                </div>
              </div>
            )
          })}

          {/* CUSTOM total mismatch warning */}
          {customSumMismatch && expenseTotal > 0 && (
            <p className="text-xs text-danger">
              Layer totals sum to {formatAmount(summary.total, currency)}, but expense total is{' '}
              {formatAmount(expenseTotal, currency)}. Difference:{' '}
              {formatAmount(Math.abs(expenseTotal - summary.total), currency)}.
            </p>
          )}
        </div>
      )}

      {/* ── Summary ──────────────────────────────────────────────────────── */}
      {showSummary && Object.keys(summary.perMember).length > 0 && (
        <div className="rounded-xl border border-gray-100 bg-gray-50 p-3">
          <p className="text-xs font-semibold uppercase tracking-widest text-gray-400 mb-2.5">
            Summary — how much each member pays
          </p>
          <div className="space-y-1.5">
            {Object.entries(summary.perMember).map(([uid, amt]) => (
              <div key={uid} className="flex items-center justify-between text-sm">
                <span className="text-gray-600 truncate">
                  {memberByUserId[uid]?.userNickname ?? memberByUserId[uid]?.name ?? uid}
                </span>
                <span className="font-medium text-gray-900 shrink-0 ml-3">
                  {formatAmount(amt, currency)}
                </span>
              </div>
            ))}
          </div>
          <div className="mt-2 pt-2 border-t border-gray-200 flex items-center justify-between text-sm font-semibold">
            <span>Total</span>
            <span className={customSumMismatch ? 'text-danger' : undefined}>
              {formatAmount(summary.total, currency)}
              {customSumMismatch && (
                <span className="ml-1 font-normal text-xs">≠ {formatAmount(expenseTotal, currency)}</span>
              )}
            </span>
          </div>
        </div>
      )}

      {error && <p className="text-sm text-danger">{error}</p>}

      {/* ── Actions ──────────────────────────────────────────────────────── */}
      {showCancel ? (
        <div className="grid grid-cols-2 gap-2">
          <Button type="button" variant="secondary" onClick={onCancel} disabled={loading}>Cancel</Button>
          <Button type="submit" loading={loading}>
            {submitLabel ?? (isEdit ? 'Save changes' : 'Add expense')}
          </Button>
        </div>
      ) : (
        <Button type="submit" className="w-full" loading={loading}>
          {submitLabel ?? (isEdit ? 'Save changes' : 'Add expense')}
        </Button>
      )}
    </form>
  )
}

import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../contexts/AuthContext'
import Input from '../../components/ui/Input'
import Button from '../../components/ui/Button'
import { getApiErrorMessage } from '../../utils/apiError'

export default function RegisterPage() {
  const navigate = useNavigate()
  const { register } = useAuth()
  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    userNickname: '',
    email: '',
    password: '',
    walletCurrencies: ['MYR'],
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  function set(field) {
    return (e) => setForm((f) => ({ ...f, [field]: e.target.value }))
  }

  function toggleCurrency(currency) {
    setForm((f) => {
      const selected = new Set(f.walletCurrencies)
      if (selected.has(currency)) selected.delete(currency)
      else selected.add(currency)
      selected.add('MYR')
      return { ...f, walletCurrencies: Array.from(selected) }
    })
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await register(form)
      navigate('/app/home', { replace: true })
    } catch (err) {
      setError(getApiErrorMessage(err, 'Registration failed'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-[100dvh] flex flex-col justify-center px-6 py-12 bg-white">
      <div className="mb-8">
        <p className="text-xs text-gray-400 uppercase tracking-widest mb-2">MyPay</p>
        <h1 className="text-2xl font-semibold text-gray-900">Create account</h1>
        <p className="text-sm text-gray-500 mt-1">Get started in seconds</p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        <Input label="First name" value={form.firstName} onChange={set('firstName')} autoComplete="given-name" required />
        <Input label="Last name" value={form.lastName} onChange={set('lastName')} autoComplete="family-name" required />
        <Input label="Nickname" value={form.userNickname} onChange={set('userNickname')} autoComplete="nickname" required />
        <Input label="Email" type="email" value={form.email} onChange={set('email')} autoComplete="email" required />
        <Input label="Password" type="password" value={form.password} onChange={set('password')} autoComplete="new-password" required />

        <div className="space-y-2">
          <p className="text-xs font-medium text-gray-500 uppercase tracking-wide">Wallets</p>
          <label className="flex items-center justify-between rounded-lg border border-[#E8E8E8] px-3 py-2.5 text-sm">
            <span className="font-medium text-gray-900">MYR</span>
            <input type="checkbox" checked disabled className="h-4 w-4" />
          </label>
          {['USD', 'SGD'].map((currency) => (
            <label key={currency} className="flex items-center justify-between rounded-lg border border-[#E8E8E8] px-3 py-2.5 text-sm">
              <span className="font-medium text-gray-900">{currency}</span>
              <input
                type="checkbox"
                checked={form.walletCurrencies.includes(currency)}
                onChange={() => toggleCurrency(currency)}
                className="h-4 w-4"
              />
            </label>
          ))}
        </div>

        {error && <p className="text-sm text-danger">{error}</p>}

        <Button type="submit" className="w-full" loading={loading}>
          Create account
        </Button>
      </form>

      <p className="mt-8 flex flex-wrap items-center gap-x-1 gap-y-2 text-sm text-gray-500">
        Already have an account?{' '}
        <Link to="/login" className="inline-flex min-h-9 items-center text-primary font-medium">
          Sign in
        </Link>
      </p>
    </div>
  )
}

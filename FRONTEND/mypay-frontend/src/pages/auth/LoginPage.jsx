import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../contexts/AuthContext'
import Input from '../../components/ui/Input'
import Button from '../../components/ui/Button'
import { getApiError, getApiErrorMessage } from '../../utils/apiError'

export default function LoginPage() {
  const navigate = useNavigate()
  const { login } = useAuth()
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login(form.email, form.password)
      navigate('/app/home', { replace: true })
    } catch (err) {
      const apiError = getApiError(err, 'Invalid credentials')
      if (err?.response?.status === 401 || apiError.code === 'UNAUTHORIZED') {
        setError(apiError.message || 'Invalid credentials')
      } else {
        setError(getApiErrorMessage(err, 'Invalid credentials'))
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-[100dvh] flex flex-col justify-center px-6 py-12 bg-white">
      <div className="mb-10">
        <p className="text-xs text-gray-400 uppercase tracking-widest mb-2">MyPay</p>
        <h1 className="text-2xl font-semibold text-gray-900">Sign in</h1>
        <p className="text-sm text-gray-500 mt-1">Manage your shared expenses</p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        <Input
          label="Email"
          type="email"
          value={form.email}
          onChange={(e) => setForm({ ...form, email: e.target.value })}
          autoComplete="email"
          required
        />
        <Input
          label="Password"
          type="password"
          value={form.password}
          onChange={(e) => setForm({ ...form, password: e.target.value })}
          autoComplete="current-password"
          required
        />

        {error && <p className="text-sm text-danger">{error}</p>}

        <Button type="submit" className="w-full" loading={loading}>
          Sign in
        </Button>
      </form>

      <p className="mt-8 flex flex-wrap items-center gap-x-1 gap-y-2 text-sm text-gray-500">
        No account?{' '}
        <Link to="/register" className="inline-flex min-h-9 items-center text-primary font-medium">
          Create one
        </Link>
      </p>
    </div>
  )
}

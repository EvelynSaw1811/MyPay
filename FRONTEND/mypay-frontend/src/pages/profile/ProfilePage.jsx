import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'

import PageLayout from '../../components/layout/PageLayout'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import { useAuth } from '../../contexts/AuthContext'

import { getProfile, updateProfile } from '../../api/profile'
import { getAccount } from '../../api/wallet'
import { getApiErrorMessage } from '../../utils/apiError'

import ProfileOverviewCard from './sections/ProfileOverviewCard'
import PersonalInfoSection from './sections/PersonalInfoSection'
import SecuritySection from './sections/SecuritySection'
import PaymentMethodsSection from './sections/PaymentMethodsSection'
import SupportSection from './sections/SupportSection'

/**
 * Profile page — composes the eight profile sections.
 *
 * This file is intentionally a thin container: data fetching and mutations
 * live here, while every visual block lives in its own `sections/*` component.
 */
export default function ProfilePage() {
  const { user, logout, applyProfile, refreshUser } = useAuth()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [saveSuccess, setSaveSuccess] = useState(null)

  // Pull fresh server-state once on mount so cached localStorage data can't go stale.
  useEffect(() => { refreshUser?.() }, [refreshUser])

  // --- Queries -----------------------------------------------------------

  const profileQuery = useQuery({
    queryKey: ['profile', user?.userId],
    queryFn:  () => getProfile(user.userId),
    enabled:  !!user?.userId,
  })

  const accountQuery = useQuery({
    queryKey: ['account'],
    queryFn:  () => getAccount().then((r) => r.data),
  })

  // --- Mutations ---------------------------------------------------------

  const updateProfileMutation = useMutation({
    mutationFn: (body) => updateProfile(user.userId, body),
    onSuccess: (fresh) => {
      applyProfile(fresh)
      queryClient.setQueryData(['profile', user.userId], fresh)
      setSaveSuccess('Profile updated successfully.')
      setTimeout(() => setSaveSuccess(null), 3000)
    },
  })

  // --- Handlers ----------------------------------------------------------

  async function handleProfileSave(diff) {
    setSaveSuccess(null)
    await updateProfileMutation.mutateAsync(diff)
  }

  async function handleLogout() {
    await logout()
    navigate('/login', { replace: true })
  }

  // --- Derived data -------------------------------------------------------

  // Prefer fresh server data; fall back to cached AuthContext user on first paint.
  const profile = profileQuery.data ?? user
  const account = accountQuery.data

  const saveError =
    (updateProfileMutation.error ? getApiErrorMessage(updateProfileMutation.error, 'Could not update profile.') : null) ??
    (updateProfileMutation.isError ? 'Could not update profile.' : null)

  // --- Render -------------------------------------------------------------

  if (profileQuery.isLoading && !profile) {
    return (
      <PageLayout title="Profile">
        <LoadingSpinner fullPage />
      </PageLayout>
    )
  }

  return (
    <PageLayout title="Profile">
      <div className="px-4 py-5 space-y-6">
        <ProfileOverviewCard user={profile} wallet={account} />

        <PersonalInfoSection
          user={profile}
          onSave={handleProfileSave}
          saving={updateProfileMutation.isPending}
          saveError={saveError}
          saveSuccess={saveSuccess}
        />

        <SecuritySection onLogout={handleLogout} userId={user?.userId} lastLogin={profile?.lastLogin} />

        <PaymentMethodsSection />

        <SupportSection />
      </div>
    </PageLayout>
  )
}

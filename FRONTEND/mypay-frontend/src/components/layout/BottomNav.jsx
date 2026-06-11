import { NavLink } from 'react-router-dom'

const HomeIcon = ({ active }) => (
  <svg className="w-5 h-5" fill={active ? 'currentColor' : 'none'} stroke="currentColor" strokeWidth="1.75" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
  </svg>
)
const CollectionsIcon = ({ active }) => (
  <svg className="w-5 h-5" fill={active ? 'currentColor' : 'none'} stroke="currentColor" strokeWidth="1.75" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
  </svg>
)
const WalletIcon = ({ active }) => (
  <svg className="w-5 h-5" fill={active ? 'currentColor' : 'none'} stroke="currentColor" strokeWidth="1.75" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
  </svg>
)
const ReportsIcon = ({ active }) => (
  <svg className="w-5 h-5" fill={active ? 'currentColor' : 'none'} stroke="currentColor" strokeWidth="1.75" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
  </svg>
)
const ProfileIcon = ({ active }) => (
  <svg className="w-5 h-5" fill={active ? 'currentColor' : 'none'} stroke="currentColor" strokeWidth="1.75" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
  </svg>
)

const tabs = [
  { to: '/app/home',        label: 'Home',        Icon: HomeIcon },
  { to: '/app/collections', label: 'Collection',   Icon: CollectionsIcon },
  { to: '/app/wallet',      label: 'Wallet',       Icon: WalletIcon },
  { to: '/app/reports',     label: 'Reports',      Icon: ReportsIcon },
  { to: '/app/profile',     label: 'Profile',      Icon: ProfileIcon },
]

export default function BottomNav() {
  return (
    <nav className="sticky bottom-0 z-30 bg-white border-t border-[#E8E8E8] flex safe-bottom">
      {tabs.map(({ to, label, Icon }) => (
        <NavLink
          key={to}
          to={to}
          className={({ isActive }) =>
            `flex-1 flex flex-col items-center gap-0.5 pt-2 pb-1.5 text-[10px] font-medium transition-colors ${
              isActive ? 'text-primary' : 'text-gray-400'
            }`
          }
        >
          {({ isActive }) => (
            <>
              <Icon active={isActive} />
              <span>{label}</span>
            </>
          )}
        </NavLink>
      ))}
    </nav>
  )
}

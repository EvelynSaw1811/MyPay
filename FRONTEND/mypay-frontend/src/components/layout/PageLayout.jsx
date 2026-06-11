import BottomNav from './BottomNav'
import TopBar from './TopBar'

export default function PageLayout({ title, back, backTo, titleMeta, actions, hideNav, children }) {
  return (
    <div className="flex flex-col min-h-[100dvh]">
      <TopBar title={title} back={back} backTo={backTo} titleMeta={titleMeta} actions={actions} />
      <main className="flex-1 overflow-y-auto">{children}</main>
      {!hideNav && <BottomNav />}
    </div>
  )
}

export default function LoadingSpinner({ fullPage }) {
  if (fullPage) {
    return (
      <div className="flex flex-1 items-center justify-center min-h-[60vh]">
        <div className="h-8 w-8 border-4 border-primary/20 border-t-primary rounded-full animate-spin" />
      </div>
    )
  }
  return <div className="h-5 w-5 border-2 border-primary/20 border-t-primary rounded-full animate-spin" />
}

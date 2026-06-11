export default function Card({ children, className = '', padding = 'p-4', onClick }) {
  return (
    <div
      className={`bg-white rounded-xl shadow-card ${padding} ${onClick ? 'cursor-pointer active:opacity-80 transition-opacity' : ''} ${className}`}
      onClick={onClick}
    >
      {children}
    </div>
  )
}

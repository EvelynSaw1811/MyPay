/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        primary: { DEFAULT: '#1677FF', light: '#4096FF', dark: '#0958D9' },
        success: '#52C41A',
        danger:  '#FF4D4F',
        warning: '#FAAD14',
      },
      boxShadow: {
        card: '0 1px 4px 0 rgba(0,0,0,0.08)',
        'card-md': '0 2px 8px 0 rgba(0,0,0,0.10)',
      },
      borderColor: {
        DEFAULT: '#E8E8E8',
      },
    },
  },
  plugins: [],
}

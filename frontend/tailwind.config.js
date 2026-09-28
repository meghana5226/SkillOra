/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#f2f6ff',
          100: '#e6ecff',
          200: '#c2d0ff',
          300: '#9db3ff',
          400: '#7089ff',
          500: '#4c63f2',
          600: '#3a49d6',
          700: '#2d38ab',
          800: '#232c82',
          900: '#1a2160',
        },
        accent: {
          400: '#22d3ac',
          500: '#0fbf96',
        },
      },
      fontFamily: {
        display: ['"Sora"', 'system-ui', 'sans-serif'],
        body: ['"Inter"', 'system-ui', 'sans-serif'],
      },
      boxShadow: {
        card: '0 2px 12px rgba(26, 33, 96, 0.06)',
        'card-hover': '0 8px 28px rgba(26, 33, 96, 0.12)',
      },
      borderRadius: {
        xl2: '1.25rem',
      },
    },
  },
  plugins: [],
}

module.exports = {
  mode: 'jit',
  purge: [
    './resources/public/js/compiled/app.js',
  ],
  darkMode: 'false',
  variants: {
    extend: {
      opacity: ['disabled'],
      cursor: ['disabled']
    }
  },
  theme: {
    extend: {
      backgroundImage: {
        'gradient-radial': 'radial-gradient(var(--tw-gradient-stops))'
      },
    }
  },
  plugins: [],
}

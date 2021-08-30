module.exports = {
    mode: 'jit',
    purge: [
        './resources/public/js/compiled/*.js',
        './resources/public/*.html'
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

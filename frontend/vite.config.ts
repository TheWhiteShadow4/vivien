import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
	plugins: [
		vue(),
		vueDevTools(),
		tailwindcss(),
	],
	resolve: {
		alias: {
			'@': fileURLToPath(new URL('./src', import.meta.url)),
		},
	},
	server: {
		port: 3000, // Der Port deines Frontend Dev-Servers
		proxy: {
			// Leitet alle API-Requests transparent an Javalin weiter
			'/api': {
				target: 'http://localhost:8080',
				changeOrigin: true,
				secure: false,
			},
			'/cache': {
				target: 'http://localhost:8080',
				changeOrigin: true,
				secure: false,
			}
		}
	}
})

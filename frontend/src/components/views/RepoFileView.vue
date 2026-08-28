<!-- src/components/views/RepoFileView.vue -->
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import BaseRepoElement from '../base/BaseRepoElement.vue'
// Importiere die generierten Typen aus deiner d.ts-Datei
import type { RepositoryElement } from '@/types/vivien-generated'

const emit = defineEmits<{
	(e: 'select', element: RepositoryElement): void
	(e: 'server-error', errorObj: { message: string; stacktrace: string }): void
}>()

// Reaktiver Zustand für die API-Daten und Lade-Status
const repository = ref<RepositoryElement | null>(null)
const isLoading = ref(true)
const errorMessage = ref<string | null>(null)

let parentFolder = ref<RepositoryElement | null>(null)
let currentFolder = ref<RepositoryElement | null>(null)

function selectElement(element: RepositoryElement)
{
	emit('select', element);

	if (element.type == 'FOLDER')
	{
		currentFolder.value = element;
	}
}

// Funktion zum asynchronen Laden der Daten vom Server
const fetchRepository = async () => {
	try {
		isLoading.value = true
		errorMessage.value = null

		const response = await fetch('/api/repo')

		 if (!response.ok) {
			// Wenn der Server 500 schickt, parsen wir das ServerError-DTO
			const errorData = await response.json()
			emit('server-error', errorData)
			return
		}

		// Daten in das typisierte Ref schreiben
		repository.value = await response.json()
		currentFolder.value = repository.value;
	} catch (error) {
		// Fängt Netzwerkfehler ab (z.B. Backend komplett offline)
		console.error('Netzwerkfehler:', error)
		emit('server-error', {
			message: 'Der Server konnte nicht erreicht werden.',
			stacktrace: 'Network disconnected or server backend offline.'
		})
	} finally {
		isLoading.value = false
	}
}

// Lifecycle-Hook: Daten beim Laden der Komponente abfragen
onMounted(() => {
	fetchRepository()
})

// Strukturierte Design-Klassen aus dem vit-Theme
const tableWrapper = "w-full border border-vit-border rounded-vit-radius bg-vit-surface shadow-vit-shadow overflow-hidden"
const tableHeader = "bg-vit-bg/50 border-b border-vit-border px-4 py-3 flex justify-between items-center text-sm font-semibold text-vit-text-muted"
</script>

<template>
	<div class="flex flex-col gap-4">
		<div :class="tableWrapper">
			<!-- Tabellen-Kopf -->
			<div :class="tableHeader">
				<span>Name</span>
				<span class="w-16 text-right">Typ</span>
			</div>

			<!-- Liste der Elemente -->
			<div class="flex flex-col">
				<!-- Lade-Zustand -->
				<div v-if="isLoading" class="p-8 text-center text-vit-text-muted animate-pulse">
					Repository wird geladen...
				</div>

				<!-- Fehler-Zustand -->
				<div v-else-if="errorMessage" class="p-8 text-center text-red-500 font-medium">
					{{ errorMessage }}
				</div>

				<div v-if="currentFolder.type != 'ROOT'" class="p-8 text-center text-vit-text-muted animate-pulse">
					..
				</div>

				<!-- Falls das Verzeichnis leer ist -->
				<div v-else-if="!currentFolder || !currentFolder.children || currentFolder.children.length === 0"
					class="p-8 text-center text-vit-text-muted">
					Dieses Verzeichnis ist leer.
				</div>

				<!-- Render der einzelnen Zeilen (nur wenn Daten vorhanden) -->
				<template v-else>
					<BaseRepoElement v-for="element in currentFolder.children" :key="element.name" :name="element.name"
						:type="element.type" @click-element="selectElement(element)" />
				</template>
			</div>
		</div>
	</div>
</template>
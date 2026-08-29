<!-- src/components/views/RepoFileView.vue -->
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import BaseRepoElement from '../base/BaseRepoElement.vue'
// Importiere die generierten Typen aus deiner d.ts-Datei
import type { RepositoryView, RepositoryElement } from '@/types/vivien-generated'
import { fetchWithView } from '@/client.ts';
import TextInput from '../base/TextInput.vue';
import IconSearch from '@/icons/IconSearch.vue';

const emit = defineEmits<{
	(e: 'select', element: RepositoryElement | null): void
	(e: 'server-error', errorObj: { message: string; stacktrace: string }): void
}>()

// Reaktiver Zustand für die API-Daten und Lade-Status
const repository = ref<RepositoryView | null>(null)
const isLoading = ref(true)
const errorMessage = ref<string | null>(null)

const currentFolder = ref<RepositoryElement | null>(null)
const selectedElement = ref<RepositoryElement | null>(null)

const searchQuery = ref('')

function selectParent()
{
	if (currentFolder.value != null && currentFolder.value.parent != null)
	{
		selectedElement.value = null;
		currentFolder.value = currentFolder.value.parent;
		emit('select', null);
	}
}

function selectElement(element: RepositoryElement)
{
	if (currentFolder.value != null && element.type == 'FOLDER')
	{
		if (!element.children)
		{
			fetchRepository(element.path);
		}
		selectedElement.value = null;
		element.parent = currentFolder.value;
		currentFolder.value = element;
	}
	else
	{
		selectedElement.value = element;
		emit('select', element);
	}
}

// Funktion zum asynchronen Laden der Daten vom Server
const fetchRepository = async (path: string) => {
	try {
		isLoading.value = true
		errorMessage.value = null

		const response = await fetchWithView(`/api/repo?path=${path}`)

		 if (!response.ok) {
			// Wenn der Server 500 schickt, parsen wir das ServerError-DTO
			const errorData = await response.json()
			emit('server-error', errorData)
			return
		}

		const tree = await response.json();
		if (path == '/')
		{
			repository.value = tree
		}
		// Parent Referenz sichern
		if (currentFolder.value?.parent)
		{
			tree.parent = currentFolder.value.parent;
		}
		currentFolder.value = tree;
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
	fetchRepository('/')
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
				
				<TextInput
					class="max-w-180 mx-8"
					v-model="searchQuery"
					type="search"
					placeholder="Repository durchsuchen"
				>
				<IconSearch />
				</TextInput>
				<span class="w-16 text-right">Status</span>
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

				<!-- Render der einzelnen Zeilen (nur wenn Daten vorhanden) -->
				<template v-else-if="currentFolder">
					<div v-if="currentFolder?.parent != null">
						<BaseRepoElement
							name=".."
							type="FOLDER"
							:selected="false"
							@click-element="selectParent()" />
					</div>

					<!-- Falls das Verzeichnis leer ist -->
					<div v-if="currentFolder.children && currentFolder.children.length === 0"
						class="p-8 text-center text-vit-text-muted">
						Hier ist nix drin.
					</div>
					<BaseRepoElement
						v-for="element in currentFolder.children"
						:key="element.name"
						:name="element.name"
						:type="element.type"
						:selected="element == selectedElement"
						@click-element="selectElement(element)"
					/>
				</template>
			</div>
		</div>
	</div>
</template>
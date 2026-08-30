<!-- src/components/views/RepoFileView.vue -->
<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import BaseRepoElement from '../base/BaseRepoElement.vue'
// Importiere die generierten Typen aus deiner d.ts-Datei
import type { RepositoryRoot, RepositoryElement } from '@/types/vivien-generated'
import { fetchWithView } from '@/client.ts';
import TextInput from '../base/TextInput.vue';
import IconSearch from '@/icons/IconSearch.vue';
import BaseIconButton from '../base/BaseIconButton.vue';

const emit = defineEmits<{
	(e: 'select', element: RepositoryElement | null): void
	(e: 'server-error', errorObj: { message: string; stacktrace: string }): void
}>()

// Reaktiver Zustand für die API-Daten und Lade-Status
const repository = ref<RepositoryRoot | null>(null)
const isLoading = ref(true)
const errorMessage = ref<string | null>(null)

const previousFolder = ref<RepositoryElement | null>(null)
const currentFolder = ref<RepositoryElement | null>(null)
const selectedElement = ref<RepositoryElement | null>(null)

const searchQuery = ref('')

function selectParent()
{
	if (currentFolder.value != null)
	{
		const el = currentFolder.value;
		if (el.type == "FOLDER")
		{
			if (el.parent != null)
			{
				selectedElement.value = null;
				navigateToFolder(el.parent);
			}
			else
			{
				let parentPath = el.path.substring(0, el.path.lastIndexOf("/"));
				if (parentPath == "") parentPath = "/";
				console.log("Parent path: " + parentPath);
				fetchRepository(parentPath);
			}
		}
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

async function fetchSerach(query: string)
{
	try
	{
		isLoading.value = true
		errorMessage.value = null

		const response = await fetchWithView(`/api/repo?q=${query}`)

		if (!response.ok)
		{
			const errorData = await response.json()
			emit('server-error', errorData)
			return
		}

		const elementa = await response.json();
		let serachRoot: RepositoryElement = { name: query, path: "", type: "VIRTUAL" };
		serachRoot.children = elementa;

		if (currentFolder.value?.type != "VIRTUAL")
		{
			previousFolder.value = currentFolder.value;
		}
		navigateToQuery(serachRoot, query)
	}
	finally
	{
		isLoading.value = false
	}
}

function clearSerach()
{
	if (previousFolder.value != null)
	{
		currentFolder.value = previousFolder.value;
	}
}

// Funktion zum asynchronen Laden der Daten vom Server
async function fetchRepository(path: string)
{
	try
	{
		isLoading.value = true
		errorMessage.value = null

		const response = await fetchWithView(`/api/repo?path=${path}`)

		if (!response.ok)
		{
			const errorData = await response.json()
			emit('server-error', errorData)
			return
		}

		const tree: RepositoryElement = await response.json();
		if (tree.type == "ROOT")
		{
			repository.value = tree
		}
		// Parent Referenz sichern
		if (currentFolder.value?.parent)
		{
			tree.parent = currentFolder.value.parent;
		}
		navigateToFolder(tree);
	}
	catch (error)
	{
		// Fängt Netzwerkfehler ab (z.B. Backend komplett offline)
		console.error('Netzwerkfehler:', error)
		emit('server-error', {
			message: 'Der Server konnte nicht erreicht werden.',
			stacktrace: 'Network disconnected or server backend offline.'
		})
	}
	finally
	{
		isLoading.value = false
	}
}

function findElementByPath(root: RepositoryElement, targetPath: string): RepositoryElement | null
{
	const normalizedTarget = targetPath.replace(/^\/|\/$/g, '')
	if (normalizedTarget === '') return root

	// Nutzt eine Breitensuche oder Tiefensuche in deinem children-Baum
	if (root.path === normalizedTarget) return root
	if (root.children)
	{
		for (const child of root.children) {
			const found = findElementByPath(child, normalizedTarget)
			if (found) return found
		}
	}
	return null
}

function navigateToFolder(folder: RepositoryElement, isBrowserBackAction = false)
{
	currentFolder.value = folder

	//var suffix = folder.path == "/" ? "" : folder.path;

	// Wenn die Aktion VOM Browser (Zurück-Taste) kam, dürfen wir keinen NEUEN Eintrag in die History pushen!
	if (!isBrowserBackAction)
	{
		const url = new URL(`${window.location.origin}${folder.path}`)

		window.history.pushState(null, folder.path, url.toString())
	}
	const titleSuffix = folder.path == "/" ? "" : folder.path;
	document.title = `Vivien ${titleSuffix}`
}

function navigateToQuery(result: RepositoryElement, query: string)
{
	currentFolder.value = result

	const url = new URL(window.location.origin)
	url.searchParams.set("q", query);
	window.history.pushState(null, query, url.toString())

	document.title = `Vivien q=${query}`
}

function handleBrowserNavigation(event: PopStateEvent)
{
	if (!repository.value) return

	// Versuche den Pfad aus dem State zu lesen, andernfalls direkt aus den Query-Parametern
	const urlParams = new URLSearchParams(window.location.search)
	const targetPath = event.state?.path ?? urlParams.get('path') ?? ''

	// Finde das passende Element im RAM-Baum
	const targetFolder = findElementByPath(repository.value, targetPath)

	if (targetFolder) {
		// Navigieren, aber pushState überspringen
		navigateToFolder(targetFolder, true)
	} else {
		// Fallback zur Wurzel, falls der Pfad (z.B. nach externem Löschen) nicht existiert
		navigateToFolder(repository.value, true)
	}
}

onMounted(() => {
	const path = window.location.pathname;
	fetchRepository(path);
	window.addEventListener('popstate', handleBrowserNavigation)
})

onUnmounted(() => {
  window.removeEventListener('popstate', handleBrowserNavigation)
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
					@enter="fetchSerach(searchQuery)"
					@clear="clearSerach()">
					<BaseIconButton @click="fetchSerach(searchQuery)">
					<IconSearch />
				</BaseIconButton>
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
					<div v-if="currentFolder?.type != 'ROOT'">
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
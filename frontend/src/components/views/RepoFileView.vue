<!-- src/components/views/RepoFileView.vue -->
<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import BaseRepoElement from '../base/BaseRepoElement.vue'
// Importiere die generierten Typen aus deiner d.ts-Datei
import type { RepositoryElement, ServerError, StageInfo } from '@/types/vivien-generated'
import { emitDisconectError, fetchWithView, uploadFiles } from '@/client.ts';
import TextInput from '../base/TextInput.vue';
import IconSearch from '@/icons/IconSearch.vue';
import BaseIconButton from '../base/BaseIconButton.vue';
import emitter from '@/mitt.ts';
import IconUpload from '@/icons/IconUpload.vue';
import IconNewFolder from '@/icons/IconNewFolder.vue';
import { useStore } from '@/store/index.ts';

const store = useStore();

const emit = defineEmits<{
	(e: 'select', element: RepositoryElement | null): void
}>()

// Reaktiver Zustand für die API-Daten und Lade-Status
//const repository = ref<RepositoryRoot | null>(null)
const isLoading = ref(true)
const errorMessage = ref<string | null>(null)

const folderCache: Map<string, RepositoryElement> = new Map([]);

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
			let parentPath = el.path.substring(0, el.path.lastIndexOf("/"));
			console.log("Parent path: " + parentPath);

			const parent = folderCache.get(parentPath);
			if (parent)
			{
				selectedElement.value = null;
				navigateToFolder(parent);
			}
			else
			{
				fetchRepository(parentPath);
			}
		}
		emit('select', null);
	}
}

function isRoot(el: RepositoryElement): boolean
{
	return el.type == 'ROOT';
}

function selectElement(element: RepositoryElement, doppelt: boolean)
{
	if (currentFolder.value != null && element.type == 'FOLDER' && doppelt)
	{
		if (!element.children)
		{
			fetchRepository(element.path);
		}
		selectedElement.value = null;
		//folderCache.value.set(parentPath);
		//element.parent = currentFolder.value;
		//currentFolder.value = element;
	}
	else
	{
		selectedElement.value = element;
		emit('select', element);
	}
}

async function fetchSearch(query: string)
{
	try
	{
		isLoading.value = true
		errorMessage.value = null

		const response = await fetchWithView(`/api/repo?q=${query}`)

		if (!response.ok)
		{
			const errorData = await response.json();
			emitter.emit('error', errorData);
			return
		}

		const elementa = await response.json();
		const serachRoot: RepositoryElement = { name: query, path: "", type: "VIRTUAL" };
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
			if (response.status == 500)
			{
				const errorData = await response.json();
				emitter.emit("error", errorData);
			}
			else
			{
				emitDisconectError(response.statusText);
			}
			return;
		}

		const tree: RepositoryElement = await response.json();
		/*if (tree.type == "ROOT")
		{
			repository.value = tree
		}*/
		folderCache.set(tree.path, tree);
		navigateToFolder(tree);
	}
	finally
	{
		isLoading.value = false
	}
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
	if (folderCache.size == 0) return

	// Versuche den Pfad aus dem State zu lesen, andernfalls direkt aus den Query-Parametern
	const urlParams = new URLSearchParams(window.location.search)
	const targetPath = event.state?.path ?? urlParams.get('path') ?? ''

	// Finde das passende Element im RAM-Baum
	let targetFolder = folderCache.get(targetPath);

	if (targetFolder)
	{
		// Navigieren, aber pushState überspringen
		navigateToFolder(targetFolder, true)
	}
	else
	{
		targetFolder = folderCache.get("/");
		if (!targetFolder) return;
		// Fallback zur Wurzel, falls der Pfad (z.B. nach externem Löschen) nicht existiert
		navigateToFolder(targetFolder, true)
	}
}

const fileInput = ref<HTMLInputElement | null>(null);

async function handleFileChange(event: Event)
{
	if (!currentFolder.value) return;

	const success = await uploadFiles(event, currentFolder.value.path);
}

function openFileBrowser()
{
	fileInput.value?.click();
}

function refreshFolder()
{
	if (!currentFolder.value) return;
	fetchRepository(currentFolder.value.path);
}

onMounted(() => {
	const path = window.location.pathname;
	fetchRepository(path);
	window.addEventListener('popstate', handleBrowserNavigation);
	emitter.on("refresh-folder", refreshFolder);
})

onUnmounted(() => {
	window.removeEventListener('popstate', handleBrowserNavigation);
	emitter.off("refresh-folder", refreshFolder);
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
					@enter="fetchSearch(searchQuery)"
					@clear="clearSerach()">
					<BaseIconButton @click="fetchSearch(searchQuery)">
					<IconSearch />
				</BaseIconButton>
				</TextInput>
				<BaseIconButton><IconNewFolder /></BaseIconButton>
				<BaseIconButton variant="primary" :disabled="!currentFolder" @click="openFileBrowser()">
					<IconUpload />
				</BaseIconButton>
				<input 
					type="file" 
					ref="fileInput" 
					style="display: none"
					multiple
					@change="handleFileChange" 
					/>
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
							label=".."
							:element="currentFolder"
							:selected="false"
							@clicked="selectParent()" />
					</div>

					<!-- Falls das Verzeichnis leer ist -->
					<div v-if="currentFolder.children && currentFolder.children.length === 0"
						class="p-8 text-center text-vit-text-muted">
						Hier ist nix drin.
					</div>
					<BaseRepoElement
						v-for="el in currentFolder.children"
						:key="el.name"
						:element="el"
						:selected="el == selectedElement"
						@clicked="selectElement(el, $event)"
					/>
				</template>
			</div>
		</div>
	</div>
</template>
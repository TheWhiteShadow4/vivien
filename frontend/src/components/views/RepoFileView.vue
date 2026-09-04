<!-- src/components/views/RepoFileView.vue -->
<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import BaseRepoElement from '../base/BaseRepoElement.vue'
// Importiere die generierten Typen aus deiner d.ts-Datei
import type { GitBranchStatus, RepositoryElement } from '@/types/vivien-generated'
import { emitDisconectError, fetchWithView, uploadFiles } from '@/client.ts';
import TextInput from '../base/TextInput.vue';
import IconSearch from '@/icons/IconSearch.vue';
import BaseIconButton from '../base/BaseIconButton.vue';
import emitter from '@/mitt.ts';
import IconUpload from '@/icons/IconUpload.vue';
import IconNewFolder from '@/icons/IconNewFolder.vue';
import Tooltip from '../base/Tooltip.vue';
import { useStore } from '@/store/index.ts';
import ListButton from '../base/ListButton.vue';
import IconBin from '@/icons/IconBin.vue';

const store = useStore();

const deleteCount = computed(() => {
	return store.git ? (store.git.missing.length + store.git.removed.length) : 0;
});

const emit = defineEmits<{
	(e: 'select', element: RepositoryElement | null): void
}>()

// Reaktiver Zustand für die API-Daten und Lade-Status
//const repository = ref<RepositoryRoot | null>(null)
const isMounted = ref(false);
const isLoading = ref(true);
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
			const parentPath = el.path.substring(0, el.path.lastIndexOf("/"));
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

function selectElement(element: RepositoryElement, doppelt: boolean)
{
	if (currentFolder.value != null && element.type == 'FOLDER' && doppelt)
	{
		const child = folderCache.get(element.path);
		if (child != null)
		{
			currentFolder.value = child;
		}
		else
		{
			fetchRepository(element.path);
		}
		selectedElement.value = null;
	}
	else
	{
		selectedElement.value = element;
		emit('select', element);
	}
}

function gitQuery(query: string): RepositoryElement
{
	const serachRoot: RepositoryElement = { name: query, path: "", type: "VIRTUAL", children: [] };
	try
	{
		if (store.git)
		{
			for(const field of query.substring(1).split(','))
			{
				const key = field.trim() as keyof GitBranchStatus;
				const list = store.git[key];
				if (Array.isArray(list))
				{
					for(const entry of list)
					{
						const name = entry.substring(entry.lastIndexOf("/"));
						const element: RepositoryElement = { name: name, path: entry, type: "FILE" };
						serachRoot.children!.push(element);
					}
				}
			}
		}
	}
	catch(err: unknown)
	{
		console.log(err);
	}
	return serachRoot;
}

async function fetchSearch(query: string)
{
	searchQuery.value = query;

	try
	{
		let serachRoot: RepositoryElement;
		if (query.startsWith(':'))
		{
			serachRoot = gitQuery(query)
		}
		else
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

			const childs = await response.json();
			serachRoot = { name: query, path: "", type: "VIRTUAL" };
			serachRoot.children = childs;
		}

		if (currentFolder.value?.type != "VIRTUAL")
		{
			previousFolder.value = currentFolder.value;
		}
		navigateToQuery(serachRoot, query);
	}
	finally
	{
		isLoading.value = false
	}
}

function clearSearch()
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
		const url = new URL(`${window.location.origin}/${folder.path}`)

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
	const targetPath = event.state?.path ?? urlParams.get('path')?.substring(1) ?? ''

	// Finde das passende Element im RAM-Baum
	let targetFolder = folderCache.get(targetPath);

	if (targetFolder)
	{
		// Navigieren, aber pushState überspringen
		navigateToFolder(targetFolder, true)
	}
	else
	{
		targetFolder = folderCache.get("");
		if (!targetFolder) return;
		// Fallback zur Wurzel, falls der Pfad (z.B. nach externem Löschen) nicht existiert
		navigateToFolder(targetFolder, true)
	}
}

const fileInput = ref<HTMLInputElement | null>(null);

async function handleFileChange(event: Event)
{
	if (!currentFolder.value) return;

	await uploadFiles(event, currentFolder.value.path);
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

function refreshFile(path: string)
{
	const d = path.lastIndexOf('/');
	const folder = (d != -1) ? path.substring(0, d) : "/";
	fetchRepository(folder);
}

onMounted(() => {
	const path = window.location.pathname.substring(1);
	fetchRepository(path);
	window.addEventListener('popstate', handleBrowserNavigation);
	emitter.on("refresh-folder", refreshFolder);
	emitter.on("refresh-file", s => refreshFile(s as string));
	isMounted.value = true;
})

onUnmounted(() => {
	window.removeEventListener('popstate', handleBrowserNavigation);
	emitter.off("refresh-folder", refreshFolder);
	emitter.off("refresh-file", s => refreshFile(s as string));
	isMounted.value = false;
})

// Strukturierte Design-Klassen aus dem vit-Theme
const tableWrapper = "w-full border border-vit-border rounded-vit-radius bg-vit-surface shadow-vit-shadow overflow-hidden"
const tableHeader = "bg-vit-bg/50 border-b border-vit-border px-4 py-3 flex justify-between items-center text-sm font-semibold text-vit-text-muted"
</script>

<template>
	<div>
		<Teleport v-if="isMounted" to="#fileview-toolbar">	
			<TextInput
				v-model="searchQuery"
				type="search"
				placeholder="Repository durchsuchen"
				@enter="fetchSearch(searchQuery)"
				@clear="clearSearch()">
				<BaseIconButton @click="fetchSearch(searchQuery)">
				<IconSearch />
			</BaseIconButton>
			</TextInput>
			<Tooltip text="Datei-Filter">
			<BaseIconButton :disabled="true"><IconNewFolder /></BaseIconButton>
			</Tooltip>
			<Tooltip text="Dateien hochladen">
			<BaseIconButton variant="primary" :disabled="!currentFolder" @click="openFileBrowser()">
				<IconUpload />
			</BaseIconButton>
			</Tooltip>
			<input 
				type="file" 
				ref="fileInput" 
				style="display: none"
				multiple
				@change="handleFileChange" 
				/>
		</Teleport>
		<Teleport v-if="isMounted" to="#papierkorb">
			<ListButton
				color="ghost"
				label="Papierkorb"
				:minified="!store.settings.sidebar"
				:disabled="isLoading && deleteCount > 0"
				:count="deleteCount"
				@click="fetchSearch(':missing,removed')">
				<IconBin />
			</ListButton>
		</Teleport>
		<div :class="tableWrapper">
			<!-- Tabellen-Kopf -->
			<div :class="tableHeader">
				<span>Name</span>
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
						:folder="currentFolder.type == 'VIRTUAL'"
						:selected="el == selectedElement"
						@clicked="selectElement(el, $event)"
					/>
				</template>
			</div>
		</div>
	</div>
</template>
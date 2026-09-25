<!-- src/components/views/RepoFileView.vue -->
<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import RepoElement from './RepoElement.vue'
import type { GitBranchStatus, RepositoryElement } from '@/types/vivien-generated'
import { emitDisconectError, fetchWithView, uploadFiles } from '@/client';
import TextInput from '../base/TextInput.vue';
import IconSearch from '@/icons/IconSearch.vue';
import BaseIconButton from '../base/BaseIconButton.vue';
import emitter from '@/mitt';
import IconUpload from '@/icons/IconUpload.vue';
import IconNewFolder from '@/icons/IconNewFolder.vue';
import Tooltip from '../base/Tooltip.vue';
import { useStore } from '@/store/index';
import ListButton from '../base/ListButton.vue';
import IconBin from '@/icons/IconBin.vue';
import NewFolderDialog from '../dialoge/NewFolderDialog.vue';
import IconAddFolder from '@/icons/IconAddFolder.vue';
import IconImport from '@/icons/IconImport.vue';
import { useGit } from '@/handler/useGit';
import IconStarFilled from '@/icons/IconStarFilled.vue';

const store = useStore();
const gitApi = useGit();

const deleteCount = computed(() => {
	return store.git ? (store.git.missing.length + store.git.removed.length) : 0;
});

const isMounted = ref(false);
const isTreeLoading = ref(true);
const errorMessage = ref<string | null>(null);

const folderCache: Map<string, RepositoryElement> = new Map([]);

const previousFolder = ref<RepositoryElement | null>(null);
const showCreateDialog = ref(false);

const searchQuery = ref('');

function selectParent()
{
	if (store.folder != null)
	{
		const el = store.folder;
		if (el.type == "FOLDER")
		{
			const parentPath = el.path.substring(0, el.path.lastIndexOf("/"));
			const parent = folderCache.get(parentPath);
			if (parent)
			{
				navigateToFolder(parent);
			}
			else
			{
				fetchRepository(parentPath);
			}
		}
		else if (el.type == "VIRTUAL")
		{
			clearSearch();
		}
	}
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
function selectElement(element: RepositoryElement, doppelt: boolean)
{
	if (store.folder != null && element.type == 'FOLDER')
	{
		const child = folderCache.get(element.path);
		if (child != null)
		{
			store.folder = child;
		}
		fetchRepository(element.path);
		//store.selected = null;
	}
	else
	{
		store.selected = element;
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
				else if (field === "fav")
				{
					for(const entry of store.settings.favorites)
					{
						serachRoot.children!.push(entry);
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
		if (query.startsWith(':') && query !== ":config")
		{
			serachRoot = gitQuery(query)
		}
		else
		{
			isTreeLoading.value = true
			errorMessage.value = null

			const response = await fetchWithView(`/api/repo?q=${query}`)

			if (!response.ok)
			{
				const errorData = await response.json();
				emitter.emit('error', errorData);
				return
			}

			serachRoot = await response.json();
		}

		if (store.folder?.type != "VIRTUAL")
		{
			previousFolder.value = store.folder;
		}
		navigateToQuery(serachRoot, query);
	}
	finally
	{
		isTreeLoading.value = false
	}
}

function clearSearch()
{
	searchQuery.value = "";
	if (previousFolder.value != null)
	{
		navigateToFolder(previousFolder.value);
	}
}

async function fetchRepository(path: string)
{
	if (store.settings.username == null) return;
	try
	{
		isTreeLoading.value = true
		errorMessage.value = null

		const response = await fetchWithView(`/api/repo?path=${path}`)

		if (!response.ok)
		{
			if (response.status == 500)
			{
				const errorData = await response.json();
				emitter.emit("error", errorData);
			}
			return;
		}

		const tree: RepositoryElement = await response.json();
		folderCache.set(tree.path, tree);
		navigateToFolder(tree);
	}
	catch(err)
	{
		emitDisconectError((err as Error).message);
	}
	finally
	{
		isTreeLoading.value = false
	}
}

function navigateToFolder(folder: RepositoryElement, isBrowserBackAction = false)
{
	store.folder = folder

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
	store.folder = result

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
	if (!store.folder) return;

	await uploadFiles(event, store.folder.path);
}

function openFileBrowser()
{
	fileInput.value?.click();
}

function refreshFolder()
{
	fetchRepository(store.folder ? store.folder.path : "");
}

async function refreshFile(path: string)
{
	const d = path.lastIndexOf('/');
	const folder = (d != -1) ? path.substring(0, d) : "/";
	await fetchRepository(folder);
	const el = store.folder?.children?.find(e => e.path == path);
	if (el != null)
	{
		store.selected = el;
	}
}

function moveClipboardFile()
{
	if (!store.clipboard || !store.folder) return;

	const file = store.clipboard.path;
	const d = file.lastIndexOf('/');
	const folder = (d != -1) ? file.substring(0, d) : "/";

	if (folder == store.folder.path) return;
	folderCache.delete(folder);

	gitApi.move(file, store.folder.path);

	store.clipboard = null;
}

onMounted(() => {
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
const tableWrapper = "w-full h-full flex flex-col border border-vit-border rounded-vit-radius bg-vit-surface shadow-vit-shadow"
const tableHeader = "bg-vit-bg/50 border-b border-vit-border px-4 py-3 flex justify-between items-center text-md font-semibold text-vit-text-muted"
</script>

<template>
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
		<BaseIconButton variant="primary" :disabled="!store.folder" @click="openFileBrowser()">
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
		<Tooltip text="Ordner erstellen">
		<BaseIconButton variant="secondary" :disabled="!store.folder" @click="showCreateDialog = true">
			<IconAddFolder />
		</BaseIconButton>
		</Tooltip>
		<div class="w-80 flex items-center gap-3">
			<template v-if="!!store.clipboard">
				<BaseIconButton variant="normal" :disabled="!store.folder" @click="moveClipboardFile()">
					<IconImport />
				</BaseIconButton>
				<span class="text-vit-text-muted">{{ store.clipboard.name }}</span>
			</template>
		</div>
	</Teleport>
	<Teleport to="body">
		<NewFolderDialog v-if="showCreateDialog" :parent="store.folder!.path" @close="showCreateDialog = false" />
	</Teleport>
	<Teleport v-if="isMounted" to="#repo-nav">
		<ListButton
			color="ghost"
			label="Papierkorb"
			:minified="!store.settings.sidebar"
			:disabled="isTreeLoading && deleteCount > 0"
			:count="deleteCount"
			@click="fetchSearch(':missing,removed')">
			<IconBin />
		</ListButton>
		<ListButton
			color="ghost"
			label="Lesezeichen"
			:minified="!store.settings.sidebar"
			:disabled="store.settings.favorites.length == 0"
			:count="store.settings.favorites.length"
			@click="fetchSearch(':fav')">
			<IconStarFilled />
		</ListButton>
	</Teleport>
	<div :class="tableWrapper">
		<!-- Tabellen-Kopf -->
		<div :class="tableHeader">
			<span>Name</span>
			<span class="w-16 text-right">Status</span>
		</div>

		<!-- Liste der Elemente -->
		<div class="flex-1 overflow-auto">
			<!-- Lade-Zustand -->
			<div v-if="!store.folder && isTreeLoading" class="p-8 text-center text-vit-text-muted animate-pulse">
				Repository wird geladen...
			</div>

			<!-- Fehler-Zustand -->
			<div v-else-if="errorMessage" class="p-8 text-center text-red-500 font-medium">
				{{ errorMessage }}
			</div>

			<!-- Render der einzelnen Zeilen (nur wenn Daten vorhanden) -->
			<template v-else-if="store.folder">
				<div v-if="store.folder?.type != 'ROOT'">
					<RepoElement
						label=".."
						:element="store.folder"
						:selected="false"
						@clicked="selectParent()" />
				</div>

				<!-- Falls das Verzeichnis leer ist -->
				<div v-if="store.folder.children && store.folder.children.length === 0"
					class="p-8 text-center text-vit-text-muted">
					Hier ist nix drin.
				</div>
				<RepoElement
					v-for="el in store.folder.children"
					:key="el.name"
					:element="el"
					:folder="store.folder.type == 'VIRTUAL'"
					:selected="el.path == store.selected?.path"
					@clicked="selectElement(el, $event)"
				/>
			</template>
		</div>
	</div>
</template>
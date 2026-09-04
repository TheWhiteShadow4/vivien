<!-- src/App.vue -->
<script setup lang="ts">

import { ref, onMounted, onUnmounted } from 'vue'
import type { FileObject, RepositoryElement, ServerError, ServerState } from './types/vivien-generated'
import ErrorBannerList from './components/ErrorBannerList.vue'
import TheHeader from './components/TheHeader.vue'
import TheSidebar from './components/TheSidebar.vue'
import RepoFileView from './components/views/RepoFileView.vue'
import ThePreviewPanel from './components/ThePreviewPanel.vue'
import { checkGitStatus, emitDisconectError, fetchWithView } from './client.ts'
import { useStore } from './store/index.ts'
import LoginDialog from './components/dialoge/LoginDialog.vue'
import CommitDialog from './components/dialoge/CommitDialog.vue'
import emitter from './mitt.ts'
import { sendFetch } from './services/git.ts'
import Splitter from './components/base/Splitter.vue'

const store = useStore();

const state = ref<ServerState>({
	user: undefined,
	view: 'Loading',
	mode: 'SETUP',
	serverErrors: []
})

const isSidebarOpen = ref(true)
const showCommitDialog = ref(false)
const isLoading = ref<boolean>(true)
const networkError = ref<string | null>(null)
const previewImage = ref<FileObject | null>(null);
const selectedElement = ref<RepositoryElement |null>(null);

async function checkBackendStatus()
{
	try
	{
		isLoading.value = true
		networkError.value = null

		const response = await fetchWithView("/api/state")

		if (!response.ok) {
			emitDisconectError(response.statusText);
			return;
		}

		// Daten reaktiv in den State schreiben
		state.value = await response.json()

		await checkGitStatus();
	}
	catch (err: unknown)
	{
		console.log(err);
	}
	finally
	{
		isLoading.value = false
	}
}

async function updatePreview(el: RepositoryElement | null)
{
	if (el == null)
	{
		selectedElement.value = null;
		return;
	}
	if (el.type != "FILE") return;

	selectedElement.value = el;

	const response = await fetchWithView(`/api/preview?file=${el.path}`);
	if (response.ok)
	{
		const fileObject: FileObject = await response.json();
		previewImage.value = fileObject;
	}
	else
	{
		previewImage.value = null;
	}
}

function onGitCommand(arg: string)
{
	console.log("onGitCommand " + arg);
	switch (arg)
	{
		case "commit": showCommitDialog.value = true; break;
		case "fetch": sendFetch(); break;
	}
}

function closeCommitDialog(needRefresh: boolean)
{
	showCommitDialog.value = false;
	if (needRefresh)
	{
		checkGitStatus();
	}
}

// Lifecycle-Hook: Wird ausgeführt, sobald die Komponente im Browser geladen ist
onMounted(() => {
	document.title = "Vivien";
	checkBackendStatus();

	emitter.on("error", (e) => state.value.serverErrors.push(e as ServerError));
	emitter.on("refresh-preview", (e) => updatePreview(e as RepositoryElement));
})

onUnmounted(() => {
	emitter.off("error", (e) => state.value.serverErrors.push(e as ServerError));
	emitter.off("refresh-preview", (e) => updatePreview(e as RepositoryElement));
})

</script>

<template>
	<div class="h-screen w-screen flex flex-col overflow-hidden select-none">

		<TheHeader :state="state" />

		<!-- Inhalt unter dem Header -->
		<div class="flex flex-1 min-h-0">

			<TheSidebar :state="state" :is-open="isSidebarOpen" @git="onGitCommand($event)" />

			<!-- Hauptbereich -->
			<main class="w-full h-full bg-vit-bg p-1">
				<Splitter>
					<template v-slot:links>
						<RepoFileView @select="(e) => updatePreview(e)" />
					</template>
					<template v-slot:rechts>
						<ThePreviewPanel :element="selectedElement" :imageData="previewImage" />
					</template>
				</Splitter>
			</main>

			<ErrorBannerList :errors="state.serverErrors"
				@dismiss-error="(index) => state.serverErrors.splice(index, 1)" />

			<!--<div class="fixed bottom-6 right-6 z-50 flex flex-row gap-4 max-w-2xl pointer-events-none">
	  <BasePanel variant="dialog" >Surface</BasePanel>
      <BasePanel variant="info" >Das ist ein Toast<br /><span class="text-vit-text-muted">Zweite Zeile.</span></BasePanel>
	  <BasePanel variant="warning" >Warning</BasePanel>
	  </div>-->
		</div>

		<CommitDialog v-if="showCommitDialog" @submit="closeCommitDialog(true)" @cancel="closeCommitDialog(false)" />
		<LoginDialog v-if="!store.settings.email" />
	</div>
</template>
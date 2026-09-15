<!-- src/App.vue -->
<script setup lang="ts">

import { ref, onMounted, onUnmounted } from 'vue'
import type { FileObject, RepositoryElement, ServerError, ServerState } from './types/vivien-generated'
import ErrorBannerList from './components/ErrorBannerList.vue'
import TheHeader from './components/TheHeader.vue'
import TheSidebar from './components/TheSidebar.vue'
import RepoFileView from './components/views/RepoFileView.vue'
import ThePreviewPanel from './components/ThePreviewPanel.vue'
import { checkGitStatus, emitDisconectError, fetchWithView, updatePreview } from './client'
import { useStore } from './store/index'
import LoginDialog from './components/dialoge/LoginDialog.vue'
import CommitDialog from './components/dialoge/CommitDialog.vue'
import emitter from './mitt'
import Splitter from './components/base/Splitter.vue'
import { useGit } from './handler/useGit'
import { getFileExtension, README_FILE, SUPPORTED_PREVIEW_TYPES } from './config.ts'

const store = useStore();

const state = ref<ServerState>({
	view: 'Loading',
	mode: 'SETUP',
	serverErrors: []
})

const isSidebarOpen = ref(true)
const showCommitDialog = ref(false)
const showLoginDialog = ref(false)
const isLoading = ref<boolean>(true)
const previewImage = ref<FileObject | null>(null);
const selectedElement = ref<RepositoryElement |null>(null);

async function checkBackendStatus()
{
	if (store.settings.username == null) return;
	try
	{
		isLoading.value = true

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

async function onRefreshPreview(el: RepositoryElement | null, select: boolean = false)
{
	if (el == null)
	{
		if (select)
			selectedElement.value = null;
		return;
	}
	if (el.type != "FILE") return;

	if (select)
		selectedElement.value = el;

	const ext = getFileExtension(el.name);
	if (ext && SUPPORTED_PREVIEW_TYPES.includes(ext))
	{
		previewImage.value = await updatePreview(el);
	}
}

const { commit } = useGit();

function onGitCommand(arg: string)
{
	console.log("onGitCommand " + arg);
	switch (arg)
	{
		case "commit": showCommitDialog.value = true; break;
		case "push": commit(""); break;
	}
}

function closeCommitDialog()
{
	showCommitDialog.value = false;
}

function closeLoginDialog(needRefresh: boolean)
{
	showLoginDialog.value = false;
	if (needRefresh)
	{
		checkBackendStatus();
		emitter.emit("refresh-folder");
	}
}

// Lifecycle-Hook: Wird ausgeführt, sobald die Komponente im Browser geladen ist
onMounted(() => {
	document.title = "Vivien";
	if (store.settings.username == null || store.settings.email == null)
	{
		showLoginDialog.value = true;
	}
	else
	{
		checkBackendStatus();
		onRefreshPreview(README_FILE as RepositoryElement, false);
	}

	emitter.on("error", (e) => state.value.serverErrors.push(e as ServerError));
	emitter.on("refresh-preview", (e) => onRefreshPreview(e as RepositoryElement));
})

onUnmounted(() => {
	emitter.off("error", (e) => state.value.serverErrors.push(e as ServerError));
	emitter.off("refresh-preview", (e) => onRefreshPreview(e as RepositoryElement));
})

</script>

<template>
	<div class="h-screen w-screen flex flex-col overflow-hidden select-none">

		<TheHeader :state="state" />

		<div class="flex flex-1 min-h-0">

			<TheSidebar :state="state" :is-open="isSidebarOpen" @git="onGitCommand($event)" @user="showLoginDialog = true" />

			<!-- Hauptbereich -->
			<main class="w-full h-full bg-vit-bg p-1">
				<Splitter>
					<template v-slot:links>
						<RepoFileView @select="(e) => onRefreshPreview(e, true)" />
					</template>
					<template v-slot:rechts>
						<ThePreviewPanel :element="selectedElement" :fileObject="previewImage" />
					</template>
				</Splitter>
			</main>

			<ErrorBannerList :errors="state.serverErrors"
				@dismiss-error="(index) => state.serverErrors.splice(index, 1)"
			/>
		</div>

		<LoginDialog v-if="showLoginDialog" @submit="closeLoginDialog(true)" @cancel="closeLoginDialog(false)" />
		<CommitDialog v-if="showCommitDialog" @submit="closeCommitDialog()" @cancel="closeCommitDialog()" />
		<LoginDialog v-if="showLoginDialog" @submit="closeLoginDialog(true)" @cancel="closeLoginDialog(false)" />
	</div>
</template>
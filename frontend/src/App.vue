<!-- src/App.vue -->
<script setup lang="ts">

import { onMounted, onUnmounted } from 'vue'
import type { RepositoryElement, ServerError } from './types/vivien-generated'
import ErrorBannerList from './components/ErrorBannerList.vue'
import TheHeader from './components/TheHeader.vue'
import TheSidebar from './components/TheSidebar.vue'
import RepoFileView from './components/views/RepoFileView.vue'
import ThePreviewPanel from './components/ThePreviewPanel.vue'
import { checkBackendStatus, checkGitStatus, sendLogin } from './client'
import { useStore } from './store/index'
import LoginDialog from './components/dialoge/LoginDialog.vue'
import CommitDialog from './components/dialoge/CommitDialog.vue'
import emitter from './mitt'
import Splitter from './components/base/Splitter.vue'
import { README_FILE, SETUP_FILE } from './config'

const store = useStore();


async function startupFunction()
{
	const path = window.location.pathname.substring(1);
	const loggedIn = await sendLogin(path);
	if (loggedIn === 502) return;
	if (typeof(loggedIn) === 'number')
	{
		store.showLoginDialog = true;
		return;
	}

	store.server = loggedIn.state;
	if (loggedIn.folder)
	{
		store.folder = loggedIn.folder;
		store.selected = null;
		if (loggedIn.selected >= 0)
		{
			store.selected = store.folder.children![loggedIn.selected]!;
		}
	}

	if (store.server?.mode == 'SETUP')
	{
		store.selected = SETUP_FILE as RepositoryElement;
	}
	else
	{
		checkGitStatus();
		if (store.selected == null)
			store.selected = README_FILE as RepositoryElement;
	}
}

function closeCommitDialog()
{
	store.showCommitDialog = false;
}

function closeLoginDialog(needRefresh: boolean)
{
	store.showLoginDialog = false;
	if (needRefresh)
	{
		checkBackendStatus().then(() => checkGitStatus());
		emitter.emit("refresh-folder");
	}
}

function handleKeyDown(event: KeyboardEvent)
{
	const activeEl = document.activeElement
	if (activeEl)
	{
		if (activeEl.tagName === 'INPUT' || activeEl.tagName === 'TEXTAREA' || activeEl.hasAttribute('contenteditable')) return;
	}

	const isModifierPressed = event.ctrlKey || event.metaKey;
	if (isModifierPressed && store.selected)
	{
		const key = event.key.toLowerCase();
		switch (key)
		{
			case 'c':
			event.preventDefault()
			store.clipboard = store.selected;
			break

			case 'x':
			event.preventDefault()
			store.clipboard = store.selected;
			break

			case 'v':
			event.preventDefault()
			emitter.emit("paste-clipboard");
			break
		}
	}
}

// Lifecycle-Hook: Wird ausgeführt, sobald die Komponente im Browser geladen ist
onMounted(() => {
	if (store.settings.username == null || store.settings.email == null)
	{
		store.showLoginDialog = true;
	}
	else
	{
		startupFunction();
	}

	emitter.on("error", (e) => store.server?.serverErrors.push(e as ServerError));
	window.addEventListener('keydown', handleKeyDown)
})

onUnmounted(() => {
	emitter.off("error", (e) => store.server?.serverErrors.push(e as ServerError));
	window.removeEventListener('keydown', handleKeyDown)
})
</script>

<template>
	<div class="h-screen w-screen flex flex-col overflow-hidden">

		<TheHeader />

		<div class="flex flex-1 min-h-0">

			<TheSidebar />

			<!-- Hauptbereich -->
			<main class="w-full h-full bg-vit-bg p-1">
				<Splitter>
					<template v-slot:links>
						<RepoFileView />
					</template>
					<template v-slot:rechts>
						<ThePreviewPanel />
					</template>
				</Splitter>
			</main>

			<ErrorBannerList />
		</div>

		<LoginDialog v-if="store.showLoginDialog" @submit="closeLoginDialog(true)" @cancel="closeLoginDialog(false)" />
		<CommitDialog v-if="store.showCommitDialog" @submit="closeCommitDialog()" @cancel="closeCommitDialog()" />
		<LoginDialog v-if="store.showLoginDialog" @submit="closeLoginDialog(true)" @cancel="closeLoginDialog(false)" />
	</div>
</template>
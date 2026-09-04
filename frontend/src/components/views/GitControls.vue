<!-- src\components\views\GitControls.vue -->
<script setup lang="ts">
import IconGitCommit from '@/icons/IconGitCommit.vue'
import IconPushStash from '@/icons/IconPushStash.vue'
import { useStore } from '@/store/index.ts'
import { computed } from 'vue'
import ListButton from '../base/ListButton.vue'
import IconGitPull from '@/icons/IconGitPull.vue'
import IconSync from '@/icons/IconSync.vue'
import { useGit } from '@/handler/useGit.ts'

const store = useStore();

const emit = defineEmits(["git", "bin"]);

const gitStageCount = computed(() => {
	return store.git ? (store.git.added.length + store.git.changed.length + store.git.removed.length) : 0;
});
const gitChangeCount = computed(() => {
	return store.git ? (store.git.added.length + store.git.changed.length + store.git.removed.length) : 0;
});
const isAdmin = computed(() => store.settings.view == "admin");


interface Props {
  variant?: "full" | "small"
}

withDefaults(defineProps<Props>(), {
  variant: "full",
})

const { fetch, reset, checkout, isLoading } = useGit();
</script>


<template>
	<nav class="flex flex-col gap-2">
		<ListButton
			v-if="isAdmin && store.git"
			variant="normal"
			color="normal"
			label="Checkout"
			:minified="variant == 'small'"
			:disabled="isLoading || gitChangeCount <= 0"
			@click="checkout(store.git.branch)">
			<IconGitPull />
		</ListButton>

		<ListButton
			v-if="store.git"
			:variant="gitChangeCount ? 'secondary' : 'normal'"
			color="accent3"
			:label="isAdmin ? 'Reset' : 'Zurücksetzen'"
			:minified="variant == 'small'"
			:disabled="isLoading || gitChangeCount <= 0"
			@click="reset()">
			<IconSync />
		</ListButton>

		<ListButton
			color="accent"
			:label="isAdmin ? 'Fetch' : 'Aktualisieren'"
			:minified="variant == 'small'"
			:disabled="isLoading"
			@click="fetch()">
			<IconGitPull />
		</ListButton>

		<ListButton
			:variant="gitStageCount ? 'primary' : 'normal'"
			color="accent2"
			:label="isAdmin ? 'Commit/Push' : 'Speichern'"
			:minified="variant == 'small'"
			:disabled="isLoading || gitStageCount <= 0"
			:count="gitStageCount"
			@click="emit('git', 'commit')">
			<IconGitCommit />
		</ListButton>

		<ListButton
			v-if="isAdmin"
			color="ghost"
			label="Stash"
			:minified="variant == 'small'"
			:disabled="isLoading"
			@click="emit('git', 'stash')">
			<IconPushStash />
		</ListButton>

		<ListButton
			v-if="isAdmin"
			color="ghost"
			label="Stash Pop"
			:minified="variant == 'small'"
			:disabled="isLoading"
			@click="emit('git', 'unstash')">
			<IconPushStash />
		</ListButton>

		<div id="papierkorb"></div>
	</nav>
</template>
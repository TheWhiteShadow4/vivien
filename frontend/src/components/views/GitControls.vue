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

// Änderungen nicht in der Stage
const gitChangeCount = computed(() => {
	return store.git ? (store.git.untracked.length + store.git.modified.length + store.git.missing.length) : 0;
});

// Änderungen in der Stage
const gitStageCount = computed(() => {
	return store.git ? (store.git.added.length + store.git.changed.length + store.git.removed.length) : 0;
});

const canCommitPush = computed(() => {
	return gitStageCount.value > 0 || store.git?.remote && store.git.remote.aheadCount > 0;
});

const isAdmin = computed(() => store.settings.view == "admin");


interface Props {
  variant?: "full" | "small"
}

withDefaults(defineProps<Props>(), {
  variant: "full",
})

const { pull, reset, checkout, isLoading } = useGit();

function commitPush()
{
	if (gitStageCount.value > 0)
		emit('git', 'commit');
	else
		emit('git', 'push')
}

</script>


<template>
	<nav class="flex flex-col gap-2">
<!-- 		<ListButton
			v-if="isAdmin && store.git"
			variant="normal"
			color="normal"
			label="Checkout"
			:minified="variant == 'small'"
			:disabled="isLoading || (gitChangeCount + gitStageCount) <= 0"
			@click="checkout(store.git.branch)">
			<IconGitPull />
		</ListButton> -->

		<ListButton
			v-if="store.git"
			:variant="(gitChangeCount + gitStageCount) > 0 ? 'secondary' : 'normal'"
			color="accent3"
			:label="isAdmin ? 'Reset' : 'Zurücksetzen'"
			:minified="variant == 'small'"
			:disabled="isLoading || (gitChangeCount + gitStageCount) <= 0"
			@click="reset()">
			<IconSync />
		</ListButton>

		<ListButton
			color="accent"
			:label="isAdmin ? 'Fetch/Pull' : 'Aktualisieren'"
			:minified="variant == 'small'"
			:disabled="isLoading"
			@click="pull()">
			<IconGitPull />
		</ListButton>

		<ListButton
			:variant="canCommitPush ? 'primary' : 'normal'"
			color="accent2"
			:label="isAdmin ? 'Commit/Push' : 'Speichern'"
			:minified="variant == 'small'"
			:disabled="isLoading || !canCommitPush"
			:count="gitStageCount"
			@click="commitPush()">
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
<!-- src\components\views\GitControls.vue -->
<script setup lang="ts">
import IconGitCommit from '@/icons/IconGitCommit.vue'
import BaseButton from '../base/BaseButton.vue'
import IconGitPull from '@/icons/IconGitPull.vue'
import IconGitPush from '@/icons/IconGitPush.vue'
import IconPopStash from '@/icons/IconPopStash.vue'
import IconPushStash from '@/icons/IconPushStash.vue'
import type { GitBranchStatus } from '@/types/vivien-generated.js'
import { useStore } from '@/store/index.ts'
import { computed } from 'vue'

const store = useStore();

const commitStyle = computed(() => {
	if (store.git)
	{
		if (store.git.modified || store.git.untracked.length > 0)
		{
			return "font-bold bg-gradient-to-t from-vit-primary to-vit-primary2";
		}
		else
		{
			return "text-vit-text-muted";
		}
	}
	return "text-vit-text-muted";
});

const pushStyle = computed(() => {
	if (store.git && store.git.remote)
	{
		if (store.git.remote.behindCount || store.git.untracked.length > 0)
		{
			return "font-bold bg-gradient-to-t from-vit-primary to-vit-primary2";
		}
		else
		{
			return "text-vit-text-muted";
		}
	}
	return "text-vit-text-muted";
});


interface Props {
  variant?: "full" | "small"
}

withDefaults(defineProps<Props>(), {
  variant: "full",
})

const emit = defineEmits(["git"])


const GitButtonBase = `w-full flex items-center px-4 py-2
font-medium text-left transition-colors cursor-pointer text-base
hover:text-vit-text-main hover:bg-vit-bg`;
const GitButtonNormal = "text-vit-text-muted";

const BaseIconStyle	  = "w-8 h-8"
const FetchIconStyle  = "rounded-vit-btn-radius border border-vit-accent2 bg-vit-accent2/15"
const CommitIconStyle = "rounded-vit-btn-radius border border-vit-accent bg-vit-accent/15"
const PushIconStyle   = "rounded-vit-btn-radius border border-vit-btn-danger bg-vit-btn-danger/15"
//const StashIconStyle  = "w-8 h-8"
//const PopIconStyle	  = "w-8 h-8"
</script>


<template>
	<nav class="flex flex-col gap-2">
		<button @click="emit('git', 'fetch')"
			:class="[GitButtonBase, GitButtonNormal]">
			<IconGitPull :class="[BaseIconStyle, FetchIconStyle]" />
			<span v-if="variant == 'full'" class="ml-3">Fetch</span>
		</button>

		<button
			@click="emit('git', 'commit')"
			:class="[GitButtonBase, commitStyle]">
			<IconGitCommit :class="[BaseIconStyle, CommitIconStyle]" />
			<span v-if="variant == 'full'" class="ml-3">Commit</span>
		</button>

		<button
			@click="emit('git', 'push')"
			:class="[GitButtonBase, GitButtonNormal]">
			<IconGitPush :class="[BaseIconStyle, PushIconStyle]" />
			<span v-if="variant == 'full'" class="ml-3">Push</span>
		</button>

		<button
			@click="emit('git', 'stash')"
			:class="[GitButtonBase, GitButtonNormal]">
			<IconPushStash :class="BaseIconStyle" />
			<span v-if="variant == 'full'" class="ml-3">Stash</span>
		</button>

		<button
			@click="emit('git', 'unstash')"
			:class="[GitButtonBase, GitButtonNormal]">
			<IconPopStash :class="BaseIconStyle" />
			<span v-if="variant == 'full'" class="ml-3">Stash Pop</span>
		</button>
	</nav>
</template>
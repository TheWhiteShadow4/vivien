<!-- src\components\views\GitControls.vue -->
<script setup lang="ts">
import IconGitCommit from '@/icons/IconGitCommit.vue'
import IconGitPull from '@/icons/IconGitPull.vue'
import IconGitPush from '@/icons/IconGitPush.vue'
import IconPopStash from '@/icons/IconPopStash.vue'
import IconPushStash from '@/icons/IconPushStash.vue'
import { useStore } from '@/store/index.ts'
import { computed } from 'vue'
import CounterBadge from '../base/CounterBadge.vue'
import ListButton from '../base/ListButton.vue'

const store = useStore();

const emit = defineEmits(["git"]);

const gitChangeCount = computed(() => {
	return store.stage ? (store.stage?.added + store.stage?.removed) : 0;
});
const commitStyle = computed(() => {
	if (gitChangeCount.value > 0)
	{
		return "font-bold bg-gradient-to-t from-vit-primary to-vit-primary2 hover:bg-gradient-to-b";
	}
	else
	{
		return "text-vit-text-muted";
	}
});

// eslint-disable-next-line @typescript-eslint/no-unused-vars
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
</script>


<template>
	<nav class="flex flex-col gap-2">
		<!-- <button @click="emit('git', 'fetch')"
			:class="[GitButtonBase, GitButtonNormal]">
			<IconGitPull :class="[BaseIconStyle, FetchIconStyle]" />
			<span v-if="variant == 'full'" class="ml-3">Fetch</span>
		</button> -->

		<ListButton
		:variant="gitChangeCount ? 'primary' : 'normal'"
		color="accent2"
		label="Commit"
		:minified="variant == 'small'"
		:disabled="gitChangeCount <= 0"
		:count="gitChangeCount"
		@click="emit('git', 'commit')">
			<IconGitCommit />
		</ListButton>

		<ListButton
			color="accent3"
			label="Push"
			:minified="variant == 'small'"
			:disabled="false"
			:count="gitChangeCount"
			@click="emit('git', 'push')">
			<IconGitPush />
		</ListButton>

		<ListButton
			color="ghost"
			label="Stash"
			:minified="variant == 'small'"
			:disabled="false"
			@click="emit('git', 'stash')">
			<IconPushStash />
		</ListButton>

		<ListButton
			color="ghost"
			label="Stash Pop"
			:minified="variant == 'small'"
			:disabled="false"
			@click="emit('git', 'unstash')">
			<IconPushStash />
		</ListButton>
	</nav>
</template>
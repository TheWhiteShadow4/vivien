<!-- src\components\views\GitControls.vue -->
<script setup lang="ts">
import IconGitCommit from '@/icons/IconGitCommit.vue'
import IconGitPush from '@/icons/IconGitPush.vue'
import IconPushStash from '@/icons/IconPushStash.vue'
import { useStore } from '@/store/index.ts'
import { computed } from 'vue'
import ListButton from '../base/ListButton.vue'

const store = useStore();

const emit = defineEmits(["git"]);

const gitChangeCount = computed(() => {
	return store.stage ? (store.stage?.added + store.stage?.removed) : 0;
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
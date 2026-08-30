<!-- src\components\UserInfo.vue -->
<script setup lang="ts">
import type { ServerUser } from '@/types/vivien-generated';

const props = defineProps<{
	user: ServerUser,
	small: boolean
}>()

</script>

<template>
	<div class="border-t border-vit-border text-sm text-vit-text-muted flex items-center gap-2 p-4">
		<div class="w-8 h-8 rounded-full bg-vit-accent flex items-center justify-center text-vit-bg font-bold">
			{{ props.user.name.charAt(0).toUpperCase() }}
		</div>
		<div v-if="!props.small">
			<p class="text-vit-text-main font-medium leading-none">{{ props.user.name }}</p>
			<p class="text-xs mt-1">Artist</p>
		</div>
	</div>
</template>
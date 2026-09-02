<!-- src\components\UserInfo.vue -->
<script setup lang="ts">

const props = defineProps<{
	username: string,
	view: string,
	small: boolean
}>()

</script>

<template>
	<div class="text-vit-text-muted flex items-center gap-2 p-4">
		<div class="w-8 h-8 rounded-full bg-vit-accent flex items-center justify-center text-vit-bg font-bold">
			{{ props.username.charAt(0).toUpperCase() }}
		</div>
		<div v-if="!props.small">
			<p class="text-vit-text-main font-medium leading-none">{{ props.username }}</p>
		</div>
	</div>
</template>
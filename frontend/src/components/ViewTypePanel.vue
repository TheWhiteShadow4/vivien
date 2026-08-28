<!-- src/components/ViewTypePanel.vue -->
<script setup lang="ts">
interface Props { view: string }

withDefaults(defineProps<Props>(), { view: 'admin' })
</script>

<template>
	<div class="flex items-center gap-4">
		<div class="flex items-center gap-2 px-6 py-2 bg-vit-accent/30 border border-vit-accent rounded-vit-panel-radius text-sm">
		<span class="text-vit-text-muted">View: <strong class="text-vit-accent">{{ view }}</strong></span>
		</div>
	</div>
</template>
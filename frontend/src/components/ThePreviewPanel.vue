<!-- src\components\ThePreviewPanel.vue -->
<script setup lang="ts">
//import BaseButton from './base/BaseButton.vue'

defineProps<{
  imageData: {
	src: string,
	width: number,
	height: number
} | null
}>()

const previewContainer = "w-2/5 bg-vit-surface border-l border-vit-border flex flex-col justify-between shrink-0 p-4"
</script>

<template>
	<section :class="previewContainer">
		<img v-if="imageData" :src="imageData.src" :width="imageData.width" :height="imageData.height" />
	</section>
</template>

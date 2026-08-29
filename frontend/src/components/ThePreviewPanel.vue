<!-- src\components\ThePreviewPanel.vue -->
<script setup lang="ts">
import type { FileObject } from '@/types/vivien-generated';
import { computed } from 'vue';

//import BaseButton from './base/BaseButton.vue'

const props = defineProps<{
  imageData: FileObject | null
}>()

const previewContainer = "w-2/5 bg-vit-surface border-l border-vit-border flex flex-col justify-between shrink-0 p-1"

const filesize = computed(() => props.imageData ? Intl.NumberFormat("de-DE", { maximumFractionDigits: 1 }).format(props.imageData.metadata.size / 1024) : 0)
</script>

<template>
	<section :class="previewContainer">
		<div class="flex w-full justify-between p-2 bg-vit-accent-bg/30">
			<template v-if="imageData">
				<span><span class="text-vit-text-muted">File: </span>{{ imageData.filename }}</span>
				<span><span class="text-vit-text-muted">Type: </span>{{ imageData.metadata.mimeType }}</span>
				<span><span class="text-vit-text-muted">Breite: </span>{{ imageData.metadata.srcWidth }}</span>
				<span><span class="text-vit-text-muted">Höhe: </span>{{ imageData.metadata.srcHeight }}</span>
				<span><span class="text-vit-text-muted">Größe: </span>{{ filesize }}kb</span>
			</template>
		</div>
		<div class="flex flex-col">
			<img v-if="imageData" :src="imageData.url" :width="imageData.metadata.width" :height="imageData.metadata.height" />
			<div class="bg-vit-surface">Dummy Toolbar</div>
		</div>
	</section>
</template>

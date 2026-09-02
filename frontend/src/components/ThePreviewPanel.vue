<!-- src\components\ThePreviewPanel.vue -->
<script setup lang="ts">
import type { FileObject, RepositoryElement } from '@/types/vivien-generated';
import { computed } from 'vue';
import MarkdownView from './views/MarkdownView.vue';
import Toolbar from './views/Toolbar.vue';

//import BaseButton from './base/BaseButton.vue'

const props = defineProps<{
	element: RepositoryElement | null
	imageData: FileObject | null
}>()

const previewContainer = "w-2/5 bg-vit-surface border border-vit-border flex flex-col h-full"

const filesize = computed(() => props.imageData ? Intl.NumberFormat("de-DE", { maximumFractionDigits: 1 }).format(props.imageData.metadata.size / 1024) : 0)
</script>

<template>
	<article :class="previewContainer">
		<div class="min-h-10 flex w-full justify-between p-2 bg-vit-accent-bg/30" role="contentinfo">
			<template v-if="imageData">
				<span><span class="text-vit-text-muted">File: </span>{{ imageData.filename }}</span>
				<!-- <span><span class="text-vit-text-muted">Type: </span>{{ imageData.metadata.mimeType }}</span> -->
				<span><span class="text-vit-text-muted">Breite: </span>{{ imageData.metadata.srcWidth }}</span>
				<span><span class="text-vit-text-muted">Höhe: </span>{{ imageData.metadata.srcHeight }}</span>
				<span><span class="text-vit-text-muted">Größe: </span>{{ filesize }}kb</span>
			</template>
		</div>
		<Toolbar v-if="element" :element="element" />
		<div v-if="imageData" class="flex flex-col flex-1 min-h-0">
			<div v-if="imageData.metadata.mimeType.startsWith('image')" class="flex flex-col items-center">
				<img :src="imageData.url" :width="imageData.metadata.width" :height="imageData.metadata.height" />
			</div>

			<div v-else-if="imageData.metadata.mimeType == 'text/markdown'"
			class="flex-1 overflow-auto">
				<MarkdownView :content="imageData.url" />
			</div>

			<div v-else-if="imageData.metadata.mimeType.startsWith('text')"
			class="flex-1 overflow-auto">
				<code class="text-s">{{ imageData.url }}</code>
				
			</div>

		</div>
	</article>
</template>

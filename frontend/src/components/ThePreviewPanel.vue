<!-- src\components\ThePreviewPanel.vue -->
<script setup lang="ts">
import type { FileObject } from '@/types/vivien-generated';
import { computed } from 'vue';
import MarkdownView from './views/MarkdownView.vue';
import IconDownload from '@/icons/IconDownload.vue';
import BaseIconButton from './base/BaseIconButton.vue';
import IconUpload from '@/icons/IconUpload.vue';

//import BaseButton from './base/BaseButton.vue'

const props = defineProps<{
  imageData: FileObject | null
}>()

const previewContainer = "w-2/5 bg-vit-surface border border-vit-border flex flex-col shrink-0 p-1"

const filesize = computed(() => props.imageData ? Intl.NumberFormat("de-DE", { maximumFractionDigits: 1 }).format(props.imageData.metadata.size / 1024) : 0)
</script>

<template>
	<article :class="previewContainer">
		<div class="flex w-full justify-between p-2 bg-vit-accent-bg/30" role="contentinfo">
			<template v-if="imageData">
				<span><span class="text-vit-text-muted">File: </span>{{ imageData.filename }}</span>
				<span><span class="text-vit-text-muted">Type: </span>{{ imageData.metadata.mimeType }}</span>
				<span><span class="text-vit-text-muted">Breite: </span>{{ imageData.metadata.srcWidth }}</span>
				<span><span class="text-vit-text-muted">Höhe: </span>{{ imageData.metadata.srcHeight }}</span>
				<span><span class="text-vit-text-muted">Größe: </span>{{ filesize }}kb</span>
			</template>
		</div>
		<div v-if="imageData">
			<div v-if="imageData.metadata.mimeType.startsWith('image')" class="flex flex-col items-center">
				<img :src="imageData.url" :width="imageData.metadata.width" :height="imageData.metadata.height" />
			</div>

			<div v-else-if="imageData.metadata.mimeType == 'text/markdown'" class="flex-1 overflow-auto flex-col items-center">
				<MarkdownView :content="imageData.url" />
			</div>

			<div v-else-if="imageData.metadata.mimeType.startsWith('text')" class="flex-1 overflow-auto flex-col items-center">
				<code class="text-s">{{ imageData.url }}</code>
				
			</div>
			<nav class="bg-vit-surface w-full h-48 p-1 border border-vit-border flex gap-4 p-2">
				<BaseIconButton variant="secondary" size="xl"><IconDownload /></BaseIconButton>
				<BaseIconButton variant="secondary" size="xl"><IconUpload /></BaseIconButton>
			</nav>
		</div>	
	</article>
</template>

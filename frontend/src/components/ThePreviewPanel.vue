<!-- src\components\ThePreviewPanel.vue -->
<script setup lang="ts">
import type { FileObject, RepositoryElement } from '@/types/vivien-generated';
import { computed } from 'vue';
import MarkdownView from './views/MarkdownView.vue';
import Toolbar from './views/Toolbar.vue';
import CodeView from '@/components/views/CodeView.vue';
import { useEditorStore } from '@/store';

const editorStore = useEditorStore();

const props = defineProps<{
	element: RepositoryElement | null
	fileObject: FileObject | null
}>()

const previewContainer = "w-2/5 bg-vit-surface border border-vit-border flex flex-col h-full w-full"

const filesize = computed(() => props.fileObject ? Intl.NumberFormat("de-DE", { maximumFractionDigits: 1 }).format(props.fileObject.metadata.size / 1024) : 0);
</script>

<template>
	<article :class="previewContainer">
		<div class="min-h-10 flex w-full justify-between p-2 bg-vit-accent-bg/30" role="contentinfo">
			<template v-if="fileObject">
				<span><span class="text-vit-text-muted">File: </span>{{ fileObject.filename }}</span>
				<!-- <span><span class="text-vit-text-muted">Type: </span>{{ fileObject.metadata.mimeType }}</span> -->
				<span><span class="text-vit-text-muted">Breite: </span>{{ fileObject.metadata.srcWidth }}</span>
				<span><span class="text-vit-text-muted">Höhe: </span>{{ fileObject.metadata.srcHeight }}</span>
				<span><span class="text-vit-text-muted">Größe: </span>{{ filesize }}kb</span>
			</template>
		</div>
		<Toolbar v-if="element" :element="element" />
		<div v-if="fileObject" class="flex flex-col flex-1 min-h-0">
			<div v-if="fileObject.metadata.mimeType.startsWith('image')" class="flex flex-col items-center">
				<img :src="fileObject.url" :width="fileObject.metadata.width" :height="fileObject.metadata.height" />
			</div>

			<div v-else-if="fileObject.metadata.mimeType == 'text/markdown'"
				class="flex-1 overflow-auto">
				<MarkdownView :content="fileObject.url" />
			</div>

			<div v-else-if="fileObject.url != null && fileObject.metadata.mimeType == 'text/json'"
				class="flex-1 overflow-auto">
				<CodeView :content="fileObject.url"/>
			</div>

			<div v-else-if="fileObject.metadata.mimeType.startsWith('text')"
				class="flex-1 overflow-auto">
				<code class="text-s">{{ fileObject.url }}</code>
				
			</div>

		</div>
	</article>
</template>

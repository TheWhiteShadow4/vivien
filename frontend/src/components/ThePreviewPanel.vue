<!-- src\components\ThePreviewPanel.vue -->
<script setup lang="ts">
import type { FileObject, RepositoryElement } from '@/types/vivien-generated';
import { computed, defineAsyncComponent, ref, watch } from 'vue';
import Toolbar from './views/Toolbar.vue';
import { ALLOWED_EDITOR_TYPES, useStore, type EditorFile, type EditorTypes } from '@/store';
import { useFiles } from '@/handler/useFiles.ts';

const MarkdownView = defineAsyncComponent(() =>
  import('@/components/views/MarkdownView.vue')
)
const CodeView = defineAsyncComponent(() =>
  import('@/components/views/CodeView.vue')
)

const store = useStore();
const files = useFiles();

const props = defineProps<{
	element: RepositoryElement | null
	fileObject: FileObject | null
}>()

const codeEditorFile = ref<EditorFile | null>(null);

watch(() => props.element, (element) => {
	if (element != null)
	{
		const editorFile = store.editor;
		if (editorFile != null)
		{
			codeEditorFile.value = editorFile;
			return;
		}
	}
}, { immediate: true })

function isValidEditorType(mimeType: string | undefined): mimeType is EditorTypes
{
	return ALLOWED_EDITOR_TYPES.includes(mimeType as EditorTypes);
}

watch(() => props.fileObject, (fileObject) => {
	const mimeType = fileObject?.metadata?.mimeType;
	if (isValidEditorType(mimeType))
	{
		const editorFile = {
			path: props.element?.path,
			content: fileObject!.url,
			type: mimeType,
			isDirty: false
		} as EditorFile;
		store.editor = editorFile;
		const lockHolder = fileObject!.metadata.lockHolder;
		editorFile.readOnly = lockHolder == null || lockHolder !== store.settings.username;

		codeEditorFile.value = editorFile;
		return;
	}
	codeEditorFile.value = null;
}, { immediate: true })

async function getFileLock(lock: boolean)
{
	if (!codeEditorFile.value) return;

	if (lock)
	{
		const result = await files.lockFile(codeEditorFile.value.path);
		if (result.success)
		{
			codeEditorFile.value.readOnly = false;
		}
	}
	else
	{
		const result = await files.unlockFile(codeEditorFile.value.path);
		if (result.success)
		{
			codeEditorFile.value.readOnly = true;
		}
	}
}

const lockHolder = computed(() => props.fileObject?.metadata.lockHolder);
const isLocked = computed(() => lockHolder.value && lockHolder.value !== store.settings.username);

const editButton = computed(() => {
	if (codeEditorFile.value == null) return "hidden";
	return codeEditorFile.value.readOnly ? "edit" : "unedit";
})

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
		<Toolbar v-if="element" :element="element" :editButton="editButton" @edit="getFileLock" />
		<div v-if="isLocked" class="h-7 px-2 bg-vit-accent-bg">Die Datei ist gerade gesperrt durch <strong>{{ lockHolder }}</strong></div>
		<div v-if="fileObject" class="flex flex-col flex-1 min-h-0">
			<div v-if="fileObject.metadata.mimeType.startsWith('image')" class="flex flex-col items-center">
				<img :src="fileObject.url" :width="fileObject.metadata.width" :height="fileObject.metadata.height" />
			</div>

			<div v-else-if="fileObject.metadata.mimeType == 'text/markdown'" class="flex-1 overflow-auto">
				<MarkdownView :content="fileObject.url" />
			</div>

			<div v-else-if="codeEditorFile != null" class="flex-1 overflow-auto">
				<CodeView :file="codeEditorFile" />
			</div>

			<div v-else-if="fileObject.metadata.mimeType.startsWith('text')" class="flex-1 overflow-auto">
				<code class="text-s">{{ fileObject.url }}</code>
			</div>

		</div>
	</article>
</template>

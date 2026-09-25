<!-- src\components\ThePreviewPanel.vue -->
<script setup lang="ts">
import type { FileObject, RepositoryElement, ServerError } from '@/types/vivien-generated';
import { computed, defineAsyncComponent, onMounted, onUnmounted, ref, watch } from 'vue';
import Toolbar from './views/Toolbar.vue';
import { ALLOWED_AUDIO_TYPES, ALLOWED_EDITOR_TYPES, ALLOWED_MODEL_TYPES, useStore, type AudioType, type EditorFile, type EditorTypes, type ModelType } from '@/store';
import { useFiles } from '@/handler/useFiles';
import { updatePreview, uploadEditorContent } from '@/client';
import emitter from '@/mitt';
import FormView from '@/components/views/FormView.vue';
import { getFileExtension, SUPPORTED_PREVIEW_TYPES } from '@/config';

const MarkdownView = defineAsyncComponent(() =>
  import('@/components/views/MarkdownView.vue')
)
const CodeView = defineAsyncComponent(() =>
  import('@/components/views/CodeView.vue')
)
const MeshView = defineAsyncComponent(() =>
  import('@/components/views/MeshView.vue')
)
const AudioPlayer = defineAsyncComponent(() =>
  import('@/components/views/AudioPlayer.vue')
)

const store = useStore();
const files = useFiles();

const fileObject = ref<FileObject | null>(null);
const codeEditorFile = ref<EditorFile | null>(null);
const codeEditorModel = ref<string>("");
const isSaving = ref<boolean>(false);
const toolbarModel = ref<{edit: boolean | null}>({edit: false});


function isValidEditorType(mimeType: string | undefined): mimeType is EditorTypes
{
	return ALLOWED_EDITOR_TYPES.includes(mimeType as EditorTypes);
}

function isValid3DType(mimeType: string | undefined): mimeType is ModelType
{
	return ALLOWED_MODEL_TYPES.includes(mimeType as ModelType);
}

function isValidAudio(mimeType: string | undefined): mimeType is AudioType
{
	return ALLOWED_AUDIO_TYPES.includes(mimeType as AudioType);
}

watch(() => store.selected, async (el) => {
	refreshPreview(el);
});

async function refreshPreview(el: RepositoryElement | null)
{
	if (el == null)
	{
		fileObject.value = null;
		codeEditorFile.value = null;
		return;
	}
	if (el.type != "FILE") return;

	const ext = getFileExtension(el.name);
	console.log("refreshPreview", el, ext)
	if (ext && SUPPORTED_PREVIEW_TYPES.includes(ext))
	{
		fileObject.value = await updatePreview(el);

		if (!fileObject.value)
		{
			codeEditorFile.value = null;
			return;
		}

		const mimeType = fileObject.value.metadata?.mimeType;
		toolbarModel.value.edit = fileObject.value.fileParams ? false : null;
		if (isValidEditorType(mimeType))
		{
			const editorFile = {
				path: store.selected?.path,
				content: fileObject.value.url,
				type: mimeType,
				isDirty: false
			} as EditorFile;
			store.editor = editorFile;
			const lockHolder = fileObject.value.metadata.lockHolder;
			editorFile.readOnly = lockHolder == null || lockHolder !== store.settings.username;

			codeEditorFile.value = editorFile;
			codeEditorModel.value = editorFile.content;
			return;
		}
	}
	else
	{
		fileObject.value = null;
	}
	codeEditorFile.value = null;
}

async function saveFile(final: boolean)
{
	if (!store.editor || store.editor.readOnly || isSaving.value) return;
	try
	{
		store.editor.content = codeEditorModel.value;

		isSaving.value = true;
		const ret = await uploadEditorContent(store.editor.path, codeEditorModel.value, final);
		if (ret)
		{
			store.editor.isDirty = false;
			if (final) store.editor.readOnly = true;
		}
	}
	catch (error)
	{
		console.error("Fehler beim Hintergrund-Speichern:", error);
		emitter.emit("error", { message: "Fehler beim Speichern" } as ServerError);
	}
	finally
	{
		isSaving.value = false;
	}
}

async function getFileLock()
{
	if (!codeEditorFile.value) return;

	const result = await files.lockFile(codeEditorFile.value.path);
	if (result.success)
	{
		codeEditorFile.value.readOnly = false;
	}
}

const lockHolder = computed(() => fileObject.value?.metadata.lockHolder);
const isLocked = computed(() => lockHolder.value && lockHolder.value !== store.settings.username);
const isSetup = computed(() => store.server?.mode === 'SETUP');

const editButton = computed(() => {
	if (codeEditorFile.value == null) return "hidden";
	return codeEditorFile.value.readOnly ? "edit" : "unedit";
})

const previewContainer = "w-2/5 bg-vit-surface border border-vit-border flex flex-col h-full w-full"
const filesize = computed(() => fileObject.value ? Intl.NumberFormat("de-DE", { maximumFractionDigits: 1 }).format(fileObject.value.metadata.size / 1024) : 0);

onMounted(() => {
	emitter.on("refresh-preview", () => refreshPreview(store.selected));
})

onUnmounted(() => {
	emitter.off("refresh-preview", () => refreshPreview(store.selected));
})
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
		<Toolbar v-if="store.selected" :element="store.selected" :editButton="editButton" v-model="toolbarModel" @lock="getFileLock()" @unlock="saveFile(true)"/>
		<div v-if="isLocked" class="h-7 px-2 bg-vit-accent-bg">Die Datei ist gerade gesperrt durch <strong>{{ lockHolder }}</strong></div>
		<div v-if="isSetup" class="h-7 px-2 bg-vit-accent-bg">Server Setup Modus</div>
		<div v-if="fileObject" class="flex flex-col flex-1 min-h-0">
			<div v-if="toolbarModel.edit">
				<FormView :fileObject="fileObject" />
			</div>

			<div v-if="fileObject.metadata.mimeType.startsWith('image')" class="flex flex-col items-center">
				<img :src="fileObject.url" :width="fileObject.metadata.width" :height="fileObject.metadata.height" />
			</div>

			<div v-else-if="fileObject.metadata.mimeType == 'text/markdown' && codeEditorFile?.readOnly" class="flex-1 overflow-auto">
				<MarkdownView :content="fileObject.url" />
			</div>

			<div v-else-if="codeEditorFile != null" class="flex-1 overflow-auto">
				<CodeView :file="codeEditorFile" v-model="codeEditorModel" @save="saveFile" />
			</div>

			<div v-else-if="isValid3DType(fileObject.metadata.mimeType)" class="flex-1 overflow-auto">
				<MeshView :fileObject="fileObject" />
			</div>

			<div v-else-if="isValidAudio(fileObject.metadata.mimeType)" class="flex-1 overflow-auto">
				<AudioPlayer :url="fileObject.url" />
			</div>

			<div v-else-if="fileObject.metadata.mimeType.startsWith('text')" class="flex-1 overflow-auto">
				<code class="text-s">{{ fileObject.url }}</code>
			</div>
		</div>

	</article>
</template>

<!-- src\views\CodeView.vue -->
<script setup lang="ts">
import CodeMirror from 'vue-codemirror6';
import { json, jsonParseLinter } from "@codemirror/lang-json";
import { yaml, yamlFrontmatter } from "@codemirror/lang-yaml"
import { linter, lintGutter } from '@codemirror/lint';
import { dracula } from 'thememirror';
import { keymap } from '@codemirror/view';
import { defaultKeymap, history, historyKeymap } from '@codemirror/commands';
import { computed, ref, watch } from 'vue';
import { useEditorStore, type EditorFile } from '@/store';
import { uploadEditorContent } from '@/client';
import emitter from '@/mitt';
import type { ServerError } from '@/types/vivien-generated';

const editorStore = useEditorStore();

interface Props { file: EditorFile }

const props = withDefaults(defineProps<Props>(), {
	file: {
		path: "",
		content: "",
		type: "text/json",
		isDirty: false
	} as any
});

let active = props.file;
const model = ref<string>(active.content);

const isSaving = ref<boolean>(false);
let saveTimeout: ReturnType<typeof setTimeout> | null = null;

async function saveEditor(file: EditorFile, content: string)
{
	isSaving.value = true;
	try
	{
		const path = file.path;
		editorStore.updateContent(path, content);

		const ret = await uploadEditorContent(path, content);
		file.isDirty = !ret;
	}
	catch (error)
	{
		console.error("Fehler beim Hintergrund-Speichern:", error);
		emitter.emit("error", { message: "Fehler beim Hintergrund-Speichern" } as ServerError);
	}
	finally
	{
		isSaving.value = false;
	}
}

watch(() => props.file.path, (newPath) => {
	if (active?.isDirty && !isSaving)
	{
		if (saveTimeout) clearTimeout(saveTimeout);
		saveEditor(active, model.value);
	}
	console.log("Neue Datei", newPath, "Inhalt:", props.file.content.substring(0, 20).replace("\n", " "))
	model.value = props.file.content;
	active = props.file;
});

watch(model, (newContent) => {
	if (saveTimeout) clearTimeout(saveTimeout);

	if (newContent !== props.file.content)
	{
		props.file.isDirty = true;
		saveTimeout = setTimeout(() => {
			saveEditor(props.file, newContent);
		}, 3000);
	}
});

let editorExtensions = computed(() => {
return {
	"text/json": [
		dracula,
		json(),
		linter(jsonParseLinter()),
		lintGutter(),
		history(),
		keymap.of([
			...defaultKeymap,
			...historyKeymap
		])
	],
	"text/yaml": [
		dracula,
		yamlFrontmatter({ content: yaml() }),
		lintGutter(),
		history(),
		keymap.of([
			...defaultKeymap,
			...historyKeymap
		])
	]
}[props.file.type]});
</script>

<template>
	<CodeMirror v-model="model" :extensions="editorExtensions" />
</template>
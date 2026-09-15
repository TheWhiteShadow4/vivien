<!-- src\views\CodeView.vue -->
<script setup lang="ts">
import CodeMirror from 'vue-codemirror6';
import { json, jsonParseLinter } from "@codemirror/lang-json";
import { yaml, yamlFrontmatter } from "@codemirror/lang-yaml"
import { linter, lintGutter } from '@codemirror/lint';
import { dracula } from 'thememirror';
import { keymap } from '@codemirror/view';
import { defaultKeymap, history, historyKeymap } from '@codemirror/commands';
import { EditorState, Prec } from '@codemirror/state';
import { computed, ref, watch } from 'vue';
import { useStore, type EditorFile } from '@/store';
import { uploadEditorContent } from '@/client';
import emitter from '@/mitt';
import type { ServerError } from '@/types/vivien-generated';
import { StreamLanguage } from '@codemirror/language';
import { toml } from "@codemirror/legacy-modes/mode/toml";

const store = useStore();

interface Props { file: EditorFile }

const props = withDefaults(defineProps<Props>(), {
	file: () => ({
		path: "",
		content: "",
		type: "text/json",
		readOnly: true,
		isDirty: false
	} as EditorFile)
});

const model = ref<string>(props.file.content);

const isSaving = ref<boolean>(false);
let saveTimeout: ReturnType<typeof setTimeout> | null = null;

async function saveEditor(file: EditorFile, content: string)
{
	isSaving.value = true;
	try
	{
		file.content = content;
		store.editor = file;

		const ret = await uploadEditorContent(file.path, content);
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
	if (props.file?.isDirty && !isSaving.value)
	{
		if (saveTimeout) clearTimeout(saveTimeout);
		saveEditor(props.file, model.value);
	}
	console.log("Neue Datei", newPath, "Inhalt:", props.file.content.substring(0, 20).replace("\n", " "))
	model.value = props.file.content;
});

watch(model, (newContent) => {
	if (saveTimeout) clearTimeout(saveTimeout);

	if (newContent !== props.file.content)
	{
		// eslint-disable-next-line vue/no-mutating-props
		props.file.isDirty = true;
		saveTimeout = setTimeout(() => {
			saveEditor(props.file, newContent);
		}, 3000);
	}
});

const editorExtensions = computed(() => {
	console.log("CodeView compute:", props.file);
	const extensions = [
		dracula,
		lintGutter(), // Fehlerleiste Links
		history(),	// Undo/Redo
		keymap.of([
			...defaultKeymap,
			...historyKeymap
		])
	];
	switch (props.file.type)
	{
		case "text/json":
			extensions.push(json());
			extensions.push(linter(jsonParseLinter()));
		case "text/yaml":
			extensions.push(yamlFrontmatter({ content: yaml() }));
		case "text/toml":
			extensions.push(StreamLanguage.define(toml));
	}
	if (props.file.readOnly)
	{
		extensions.push(Prec.highest(EditorState.readOnly.of(true)));
	}
	return extensions;
});
</script>

<template>
	<CodeMirror v-model="model" :extensions="editorExtensions" />
</template>
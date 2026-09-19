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
import { computed, watch } from 'vue';
import { useStore, type EditorFile } from '@/store';
import { StreamLanguage } from '@codemirror/language';
import { toml } from "@codemirror/legacy-modes/mode/toml";

const store = useStore();

const emit = defineEmits<{
	(e: 'save', final: boolean): void
}>();

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

const model = defineModel<string>({ required: true });

let saveTimeout: ReturnType<typeof setTimeout> | null = null;

async function saveEditor(file: EditorFile, final: boolean)
{
	if (file.readOnly) return;

	file.content = model.value;
	store.editor = file;
	emit("save", final);
}

watch(() => props.file.path, (newPath) => {
	if (props.file?.isDirty)
	{
		if (saveTimeout) clearTimeout(saveTimeout);
		saveEditor(props.file, true);
	}
	console.log("Neue Datei", newPath, "Inhalt:", props.file.content.substring(0, 20).replace("\n", " "))
	//model.value = props.file.content;
});

watch(model, (newContent) => {
	if (saveTimeout) clearTimeout(saveTimeout);

	if (newContent !== props.file.content)
	{
		// eslint-disable-next-line vue/no-mutating-props
		props.file.isDirty = true;
		saveTimeout = setTimeout(() => {
			saveEditor(props.file, false);
		}, 5000);
	}
});

const editorExtensions = computed(() => {
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
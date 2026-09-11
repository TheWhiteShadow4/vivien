<!-- src\views\CodeView.vue -->
<script setup lang="ts">
import CodeMirror from 'vue-codemirror6';
import { json, jsonParseLinter } from "@codemirror/lang-json";
import { linter, lintGutter } from '@codemirror/lint';
import { dracula } from 'thememirror';
import { keymap } from '@codemirror/view';
import { defaultKeymap, history, historyKeymap } from '@codemirror/commands';


const model = defineModel<string>({ required: true });
	
const editorExtensions = [
	dracula,
	json(),
	linter(jsonParseLinter()),
	lintGutter(),
	history(),
	keymap.of([
		...defaultKeymap,
		...historyKeymap
  ])
];
</script>

<template>
	<CodeMirror v-model="model" :extensions="editorExtensions" />
</template>
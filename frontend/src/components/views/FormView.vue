<!-- src\views\FormView.vue -->
<script setup lang="ts">
import { ref } from "vue";
import { JsonForms, type JsonFormsChangeEvent } from "@jsonforms/vue";
import { vanillaRenderers } from "@jsonforms/vue-vanilla";
import schema from "./schema.json"

const renderers = Object.freeze([
  ...vanillaRenderers,
  // here you can add custom renderers
]);

const data = ref({});

const onChange = (event: JsonFormsChangeEvent) => {
  data.value = event.data;
};

</script>

<template>
	<json-forms
		:data="data"
		:renderers="renderers"
		:schema="schema"
		@change="onChange"
	/>
</template>
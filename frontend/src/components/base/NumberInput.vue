<!-- eslint-disable vue/multi-word-component-names -->
<!-- src/components/base/NumberInput.vue -->
<script setup lang="ts">

interface Props {
	label?: string
	id?: string
	disabled?: boolean
}
defineProps<Props>();

const model = defineModel<number>({ required: true });

</script>

<template>
	<div class="container">
		<label v-if="label" :for="id">{{ label }}</label>
		<input :id="id" v-model="model" type="number" :disabled="disabled" />
	</div>
</template>

<style scoped>
@import "tailwindcss";
@import "@/style.css";

.container {
	@apply relative flex items-center w-full bg-vit-surface box-content
	rounded-vit-panel-radius border transition-all duration-200
	border-vit-border hover:border-vit-btn-hover
	focus-within:border-vit-accent focus-within:ring-1 focus-within:ring-vit-accent
} 

input {
	@apply w-full bg-transparent text-vit-text-main text-base
	focus:outline-none disabled:opacity-50 disabled:cursor-not-allowed px-2
}

input {
  -webkit-appearance: textfield;
  -moz-appearance: textfield;
  appearance: textfield;
  text-align: right;
}
</style>
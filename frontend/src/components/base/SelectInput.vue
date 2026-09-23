<!-- eslint-disable vue/multi-word-component-names -->
<!-- src/components/base/SelectInput.vue -->
<script setup lang="ts">
import IconArrow from '@/icons/IconArrow.vue';
import { computed, ref } from 'vue';

const isOpen = ref<boolean>(false);

interface Props {
	label?: string
	id?: string
	disabled?: boolean
	values: string[]
}
defineProps<Props>();

const model = defineModel<number>({ required: true });

function onSelect(index: number)
{
	model.value = index;
	isOpen.value = false;
}

const containerStyle = computed(() => isOpen.value ? "" : "");
const dropdownStyle = computed(() => isOpen.value ? "" : "hidden");
</script>

<template>
	<div class="relative">
		<div class="container" :class="containerStyle" @click="isOpen = !isOpen">
			<label v-if="label" :for="id">{{ label }}</label>
			<button> {{ model !== undefined ? values[model] : "-" }} <IconArrow /></button>
		</div>
		<div class="dropdown" :class="dropdownStyle">
			<ul>
				<li v-for="(text, index) in values" :key="text" class="hover:bg-vit-bg" @click="onSelect(index)">{{ text }}</li>
			</ul>
		</div>
	</div>
</template>

<style scoped>
@import "tailwindcss";
@import "@/style.css";

.container {
	@apply flex items-center w-full bg-vit-surface box-content
	rounded-vit-panel-radius border transition-all duration-200
	border-vit-border hover:border-vit-btn-hover
	focus-within:border-vit-accent focus-within:ring-1 focus-within:ring-vit-accent
}

.dropdown {
	@apply absolute top-8 right-0 z-10
	border border-vit-border rounded-vit-panel-radius bg-vit-surface
	w-full
}

.dropdown li {
	@apply p-1
}

button {
	@apply w-full bg-transparent text-vit-text-main text-left
	flex justify-between
	focus:outline-none disabled:opacity-50 disabled:cursor-not-allowed pl-2
}

svg {
	@apply h-7
}
</style>
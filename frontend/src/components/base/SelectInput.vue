<!-- eslint-disable vue/multi-word-component-names -->
<!-- src/components/base/SelectInput.vue -->
<script setup lang="ts">
import IconArrow from '@/icons/IconArrow.vue';
import { computed, onMounted, onUnmounted, ref } from 'vue';

const isOpen = ref<boolean>(false);
const dropdownRef = ref<HTMLElement | null>(null);

interface Props {
	mode: "value" | "index"
	label?: string
	id?: string
	disabled?: boolean
	values: string[]
}
const props = defineProps<Props>();

const model = defineModel<string | number>({ required: true });

function onSelect(index: number)
{
	if (props.mode === "value")
		model.value = props.values[index] as string;
	else
		model.value = index;
	isOpen.value = false;
}

const currentValue = computed(() => {
	if (props.mode === "value")
		return model.value;
	else
		return props.values[model.value as number];
});

const containerStyle = computed(() => isOpen.value ? "" : "");
const dropdownStyle = computed(() => isOpen.value ? "" : "hidden");

const handleClickOutside = (event: MouseEvent) => {
	if (dropdownRef.value && !dropdownRef.value.contains(event.target as Node)) isOpen.value = false
}

onMounted(() => document.addEventListener('click', handleClickOutside))
onUnmounted(() => document.removeEventListener('click', handleClickOutside))
</script>

<template>
	<div class="relative" ref="dropdownRef">
		<div class="container" :class="containerStyle" @click="isOpen = !isOpen">
			<label v-if="label" :for="id">{{ label }}</label>
			<button> {{ currentValue }} <IconArrow /></button>
		</div>
		<div class="dropdown" :class="dropdownStyle">
			<ul>
				<template v-for="(text, index) in values" :key="text">
				<li v-if="!text.startsWith('-')" class="hover:bg-vit-bg" @click="onSelect(index)">{{ text }}</li>
				</template>
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
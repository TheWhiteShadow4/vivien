<!-- src/components/base/BaseButton.vue -->
<script setup lang="ts">
import { computed } from 'vue';

interface Props {
	label: string,
	selected?: boolean
}

const props = withDefaults(defineProps<Props>(), {
	label: "",
	selected: false
})

defineEmits<{
	(e: 'click', event: MouseEvent): void
}>()

const baseStyle = "border-vit-border";
const dynStyle = computed(() => {
	return props.selected ? "border-x border-t" : "border"
});


</script>

<template>
	<div :class="[baseStyle, dynStyle]">
		<p>{{ label }}</p>
	</div>
</template>
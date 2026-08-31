<!-- src\components\base\ListButton.vue -->
<script setup lang="ts">
import { computed } from 'vue'
import CounterBadge from './CounterBadge.vue'

interface Props {
  variant?: "primary" | "secondary" | "danger" | "normal"
  color?: "normal" | "accent" | "accent2" | "accent3" | "ghost"
  minified?: boolean
  disabled?: boolean
  label: string,
  count?: number
}

const props = withDefaults(defineProps<Props>(), {
	variant: "normal",
	color: "ghost",
	minified: false,
	disabled: false,
	label: "",
	count: 0
})

const emit = defineEmits(["click"]);

const BaseStyle = `w-full flex items-center px-4 py-2
font-medium text-left transition-colors cursor-pointer text-base`;
const VariantStyle = computed(() => {
	if (props.disabled)
	{
		return "text-vit-text-muted hover:text-vit-text-muted";
	}
	else return {
		primary: `bg-vit-primary text-vit-text-main font-bold shadow-vit-inset hover:bg-gradient-to-b
		active:translate-y-[2px] bg-gradient-to-t from-vit-primary to-vit-primary2`,

		secondary: `bg-vit-secondary/20 text-vit-text-main active:translate-y-[1px]
		hover:bg-vit-secondary/50 hover:text-vit-text-main`,

		danger: `bg-vit-btn-danger text-vit-bg font-bold active:translate-y-[1px]
		hover:bg-vit-btn-danger-hover`,

		normal: `text-vit-text-muted hover:bg-vit-bg active:translate-y-[1px]
		hover:text-vit-text-main`
	}[props.variant];
});

const BaseIconStyle = "w-8 h-8 rounded-full"
const IconStyle = computed(() => {
	if (props.disabled)
	{
		return props.color == "ghost" ? "" : "border text-vit-text-muted";
	}
	else return {
		accent: `border border-vit-accent2 bg-vit-accent2/15`,
		accent2: `border border-vit-accent bg-vit-accent/15`,
		accent3: `border border-vit-btn-danger bg-vit-btn-danger/15`,
		normal: `border border-vit-border bg-vit-bg/15`,
		ghost: ""
	}[props.color];
});

</script>

<template>
	<button
		@click="emit('click')"
		:disabled="disabled"
		:class="[BaseStyle, VariantStyle, 'relative']">
		<div :class="[BaseIconStyle, IconStyle]"><slot /></div>
		<CounterBadge
			v-if="count > 0"
			class="absolute left-7 top-4.5"
			:count="count"
		/>
		<span v-if="!minified" :class="['ml-3']">{{ label }}</span>
	</button>
</template>
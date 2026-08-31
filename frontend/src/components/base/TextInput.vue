<script setup lang="ts">
import IconClose from '@/icons/IconClose.vue'
import { computed } from 'vue'
import BaseIconButton from './BaseIconButton.vue'

// Definiere die unterstützten Varianten für die Texteingabe
export type InputVariant = 'default' | 'success' | 'failed'

interface Props {
	modelValue: string
	type?: 'text' | 'search' | 'password'
	placeholder?: string
	label?: string
	id?: string
	disabled?: boolean
	variant?: InputVariant
}

const props = withDefaults(defineProps<Props>(), {
	type: 'text',
	placeholder: '',
	disabled: false,
	variant: 'default'
})

const emit = defineEmits<{
	(e: 'update:modelValue', value: string): void
	(e: 'enter'): void
	(e: 'clear'): void
}>()

// Lokales Binding für das Input-Feld
const value = computed({
	get: () => props.modelValue,
	set: (newValue) => emit('update:modelValue', newValue)
})

// REGLER 5: Design-Entscheidungen (Farben, Radien, Rahmen) ausgelagert
const containerStyles = 'relative flex items-center w-full bg-vit-surface rounded-vit-panel-radius border transition-all duration-200'

// REGLER 4 & 6: Varianten-Muster mit visuellem Feedback für Focus und Hover
const variantStyles: Record<InputVariant, string> = {
	default: 'border-vit-border hover:border-vit-btn-hover focus-within:border-vit-accent focus-within:ring-1 focus-within:ring-vit-accent',
	success: 'border-vit-succes focus-within:ring-1 focus-within:ring-vit-succes',
	failed: 'border-vit-failed focus-within:ring-1 focus-within:ring-vit-failed',
}

const inputStyles = 'w-full bg-transparent text-vit-text-main text-base focus:outline-none disabled:opacity-50 disabled:cursor-not-allowed pl-1'

const labelStyles = 'text-sm font-medium text-vit-text-muted select-none'

function handleKeyDownEnter() {
	emit('enter')
}

function clearInput() {
	if (props.disabled) return
	value.value = ''
	emit('clear')
}
</script>

<template>
	<div class="flex flex-col gap-1.5 w-full">

		<!-- Optionales Label über dem Input -->
		<label v-if="label" :for="id" :class="labelStyles">
			{{ label }}
		</label>

		<!-- Der Input-Container umschließt das Feld und optionale Icons (z.B. Lupe oder Clear-Button) -->
		<div :class="[containerStyles, variantStyles[variant], props.disabled ? 'opacity-60' : '']">

			<!-- Slot für ein Icon am Anfang (z.B. Suchlupe) -->
			<div class="flex items-center pl-1">
				<slot />
			</div>

			<input :id="id" v-model="value" :type="type" :placeholder="placeholder" :disabled="disabled"
				:class="[inputStyles, (type === 'search') ? 'pr-2' : 'pr-3', 'py-2']"
				@keydown.enter="handleKeyDownEnter" />

			 <BaseIconButton
			 	v-if="type === 'search' && value"
				class="flex items-center"
				@click="clearInput">
				<IconClose />
			</BaseIconButton>
		</div>
	</div>
</template>
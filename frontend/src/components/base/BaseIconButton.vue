<!-- src/components/base/BaseIconButton.vue -->
<script setup lang="ts">
interface Props {
  variant?: 'primary' | 'secondary' | 'danger' | 'normal'
  size?: 'sm' | 'md' | 'lg' |'xl'
  disabled?: boolean
}

withDefaults(defineProps<Props>(), {
  variant: 'normal',
  size: 'md',
  disabled: false
})

defineEmits<{
  (e: 'click', event: MouseEvent): void
}>()

// Basis-Design (Identisch zum Button, aber erzwungenes quadratisches Seitenverhältnis)
const baseStyles = `inline-flex items-center justify-center aspect-square rounded-vit-btn-radius
rounded-vit-radius transition-all duration-200 cursor-pointer select-none
focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-vit-primary
disabled:opacity-40 disabled:cursor-not-allowed`

const variantStyles = {
  primary: "bg-gradient-to-t from-vit-primary to-vit-primary2 text-vit-text-main hover:bg-gradient-to-b active:scale-95 shadow-vit-shadow",
  secondary: "bg-vit-secondary/10 text-vit-text-main border border-vit-secondary hover:bg-vit-secondary/50 active:scale-95 shadow-vit-shadow",
  danger: "bg-vit-btn-danger text-vit-bg hover:border-vit-danger-hover hover:bg-vit-btn-danger-hover active:scale-95",
  normal: "bg-transparent text-vit-text-muted hover:text-vit-text-main hover:border-vit-btn-hover border border-transparent"
}

// Exakt quadratische Größen-Definitionen
const sizeStyles = {
  sm: "w-7 h-7 p-1 text-sm",
  md: "w-9 h-9 p-1.5 text-base",
  lg: "w-12 h-12 p-2 text-lg",
  xl: "w-20 h-20 p-2 text-xl"
}
</script>

<template>
  <button
    :type="'button'"
    :disabled="disabled"
    :class="[baseStyles, variantStyles[variant], sizeStyles[size]]"
    @click="$emit('click', $event)"
  >
    <!-- Slot nimmt exakt ein Icon auf (z.B. ein SVG oder eine Icon-Komponente) -->
    <slot />
  </button>
</template>
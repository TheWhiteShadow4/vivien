<!-- src/components/base/BaseButton.vue -->
<script setup lang="ts">
interface Props {
  variant?: 'primary' | 'secondary' | 'danger' | 'normal'
  size?: 'sm' | 'md' | 'lg'
  disabled?: boolean
}

// Standardwerte definieren
withDefaults(defineProps<Props>(), {
  variant: 'primary',
  size: 'md',
  disabled: false
})

defineEmits<{
  (e: 'click', event: MouseEvent): void
}>()

const baseStyles = `inline-flex items-center justify-center
rounded-vit-btn-radius transition-colors duration-200
cursor-pointer select-none focus-visible:outline-none focus-visible:ring-2
focus-visible:ring-vit-primary focus-visible:ring-offset-2 disabled:opacity-50
disabled:cursor-not-allowed disabled:shadow-none`

const variantStyles = {
  primary: `bg-vit-primary text-vit-text-main font-bold shadow-vit-inset hover:bg-gradient-to-b
  active:translate-y-[2px] primary-gradient`,

  secondary: `bg-vit-secondary/10 border-vit-secondary text-vit-text-main border
  shadow-vit-shadow hover:bg-vit-secondary/50 active:translate-y-[2px]`,

  danger: `bg-vit-btn-danger text-vit-bg font-bold shadow-vit-inset
  hover:bg-vit-btn-danger-hover active:translate-y-[2px]`,

  normal: `shadow-vit-shadow bg-vit-btn text-vit-text-main hover:bg-vit-btn-hover
  shadow-none border border-transparent active:translate-y-[2px]`
}

// Größen-Varianten für konsistente Abstände
const sizeStyles = {
  sm: "px-3 py-1.5 text-xs gap-1.5",
  md: "px-4 py-2 text-sm gap-2",
  lg: "px-5 py-2.5 text-base gap-2.5"
}
</script>

<template>
  <button
    :type="'button'"
    :disabled="disabled"
    :class="[baseStyles, variantStyles[variant], sizeStyles[size]]"
    @click="$emit('click', $event)"
  >
    <!-- Slot für Text und optionale Icons -->
    <slot />
  </button>
</template>
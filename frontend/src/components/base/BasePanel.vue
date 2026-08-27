<!-- src/components/base/BasePanel.vue -->
<script setup lang="ts">
interface Props {
  variant?: 'dialog' | 'info' | 'warning'
  elevation?: 'none' | 'normal' | 'high'
}

withDefaults(defineProps<Props>(), {
  variant: 'dialog',
  elevation: 'high'
})

const baseStyles = `w-full border rounded-vit-popup-radius py-2 px-4
transition-all duration-200`

const variantStyles = {
  // Der Standard: Für normale UI-Blöcke, Tabellen-Rahmen oder Standard-Dialoge
  dialog: "bg-vit-surface/80 border-vit-border text-vit-text-main",
  
  // Für Info-Toasts
  info: "bg-vit-accent-bg/50 border-vit-accent/50 text-vit-text-main",

  // Für Fehlermeldungen
  warning: "bg-vit-surface/80 border-vit-danger border-2 text-vit-text-main",
}

// Schatten-Hierarchie für visuelle Tiefe
const elevationStyles = {
  none: "shadow-none",
  normal: "shadow-vit-shadow",
  // Für Popups/Dialoge, die optisch weit "über" dem Content schweben müssen
  high: "shadow-xl shadow-vit-shadow"
}
</script>

<template>
   <div :class="[baseStyles, variantStyles[variant], elevationStyles[elevation]]">
   <slot />
  </div>
</template>
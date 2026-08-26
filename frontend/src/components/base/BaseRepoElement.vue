<!-- src/components/base/BaseRepoElement.vue -->
<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps({
  name: { type: String, required: true },
  type: { type: String, required: true } // "FOLDER" oder "FILE"
})

defineEmits(['click-element'])

// Visuelle Stile aus dem vit-Theme
const rowContainer = "w-full flex items-center justify-between p-4 bg-vit-surface hover:bg-vit-bg/40 border-b border-vit-border transition-colors cursor-pointer text-base"
const iconStyle = "text-xl select-none"
const nameStyle = "font-medium text-vit-text-main"

// Typ-spezifische Farben und Icons
const isFolder = computed(() => props.type === 'FOLDER')
const typeLabel = computed(() => isFolder.value ? 'Ordner' : 'Datei')
const typeBadge = computed(() => {
  return isFolder.value 
    ? "bg-vit-secondary/10 text-vit-secondary border border-vit-secondary/20"
    : "bg-vit-primary/10 text-vit-primary border border-vit-primary/20"
})
</script>

<template>
  <div :class="rowContainer" @click="$emit('click-element')">
    <!-- Linke Seite: Icon & Name -->
    <div class="flex items-center gap-4">
      <!-- Visuelles Feedback für den Typ -->
      <span :class="iconStyle">
        {{ isFolder ? '📁' : '📄' }}
      </span>
      
      <span :class="nameStyle">{{ name }}</span>
    </div>

    <!-- Rechte Seite: Typ-Badge (Tablet-optimiert lesbar) -->
    <span class="px-3 py-1 rounded-vit-radius text-xs font-mono" :class="typeBadge">
      {{ typeLabel }}
    </span>
  </div>
</template>
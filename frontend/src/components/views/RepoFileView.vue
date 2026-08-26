<!-- src/components/views/RepoFileView.vue -->
<script setup lang="ts">
import BaseRepoElement from '../base/BaseRepoElement.vue'

defineProps({
  repository: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['select'])

// Strukturierte Design-Klassen aus dem vit-Theme
const tableWrapper = "w-full border border-vit-border rounded-vit-radius bg-vit-surface shadow-vit-shadow overflow-hidden"
const tableHeader = "bg-vit-bg/50 border-b border-vit-border px-4 py-3 flex justify-between items-center text-sm font-semibold text-vit-text-muted"
</script>

<template>
  <div class="flex flex-col gap-4">
    <div :class="tableWrapper">
      <!-- Tabellen-Kopf -->
      <div :class="tableHeader">
        <span>Name</span>
        <span class="w-16 text-right">Typ</span>
      </div>

      <!-- Liste der Elemente -->
      <div class="flex flex-col">
        <!-- Falls das Verzeichnis leer ist -->
        <div v-if="!repository.elements || repository.elements.length === 0" class="p-8 text-center text-vit-text-muted">
          Dieses Verzeichnis ist leer.
        </div>

        <!-- Render der einzelnen Zeilen -->
        <BaseRepoElement
          v-for="element in repository.elements"
          :key="element.name"
          :name="element.name"
          :type="element.type"
          @click-element="emit('select', element)"
        />
      </div>
    </div>
  </div>
</template>
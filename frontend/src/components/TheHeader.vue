<script setup lang="ts">
import IconVivien from '../icons/IconVivien.vue'

const headerContainer = "h-14 w-full bg-vit-surface border-b border-vit-border px-6 flex items-center justify-between z-20 shrink-0"
</script>

<template>
  <header :class="headerContainer">
    <!-- Logo und echter Anwendungsname "Vivien" -->
    <div class="flex items-center gap-3">
        <IconVivien />
	    <span class="text-lg font-bold tracking-wide bg-gradient-to-r from-purple-400 to-pink-500 bg-clip-text text-transparent">
          Vivien
        </span>
    </div>

    <!-- Rechte Seite: Projekt-Status -->
    <div class="flex items-center gap-4">
      <div class="flex items-center gap-2 px-3 py-1 bg-vit-bg border border-vit-border rounded-vit-radius text-sm">
        <span class="w-2 h-2 rounded-full bg-vit-primary"></span>
        <span class="text-vit-text-muted">Projekt: <strong class="text-vit-text-main">RPG-Maker-Jam</strong></span>
      </div>
    </div>
  </header>
</template>
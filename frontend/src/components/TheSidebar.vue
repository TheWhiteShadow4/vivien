<!-- src/components/layout/TheSidebar.vue -->
<script setup lang="ts">
defineProps({
  activeView: String
})

const emit = defineEmits(['navigate'])

const sidebarContainer = "w-64 bg-vit-surface border-r border-vit-border flex flex-col justify-between shrink-0 p-4"
const navButtonBase = "w-full flex items-center gap-3 px-4 py-3 rounded-vit-radius font-medium text-left transition-colors cursor-pointer text-base"

// Farbzustände basierend auf dem "vit"-Theme
const navButtonStyles = (isActive: boolean) => {
  return isActive 
    ? "bg-vit-primary text-vit-bg font-bold shadow-vit-shadow" 
    : "text-vit-text-muted hover:text-vit-text-main hover:bg-vit-bg/50"
}
</script>

<template>
  <aside :class="sidebarContainer">
    <!-- Navigation -->
    <nav class="flex flex-col gap-2">
      <button 
        @click="emit('navigate', 'dashboard')"
        :class="[navButtonBase, navButtonStyles(activeView === 'dashboard')]"
      >
        <span>📊</span> Dashboard
      </button>
      
      <button 
        @click="emit('navigate', 'assets')"
        :class="[navButtonBase, navButtonStyles(activeView === 'assets')]"
      >
        <span>🎨</span> Assets (Artists)
      </button>
      
      <button 
        @click="emit('navigate', 'story')"
        :class="[navButtonBase, navButtonStyles(activeView === 'story')]"
      >
        <span>✍️</span> Story & Skripte
      </button>
    </nav>

    <!-- Benutzer / Rolle -->
    <div class="border-t border-vit-border pt-4 text-sm text-vit-text-muted flex items-center gap-2">
      <div class="w-8 h-8 rounded-full bg-vit-accent flex items-center justify-center text-vit-bg font-bold">
        A
      </div>
      <div>
        <p class="text-vit-text-main font-medium leading-none">Alex (Artist)</p>
        <p class="text-xs mt-1">Kein Git benötigt</p>
      </div>
    </div>
  </aside>
</template>
<!-- src/components/layout/TheSidebar.vue -->
<script setup lang="ts">
import BaseButton from './base/BaseButton.vue'

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

const styleguideHeading = "text-xs font-semibold text-vit-text-muted uppercase tracking-wider"
// Layout-Klassen für das responsive Zweier-Grid
const gridLayout = "grid grid-cols-2 gap-3"
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

	<div class="flex flex-col gap-3">
        <h3 :class="styleguideHeading">Base Buttons</h3>
        
        <!-- 4. Zweierreihen über CSS Grid -->
        <div :class="gridLayout">
		<BaseButton variant="normal">Normal</BaseButton>
		<BaseButton variant="primary">Primary</BaseButton>
		<BaseButton variant="secondary">Secondary</BaseButton>
		<BaseButton variant="danger">Warnung</BaseButton>
		</div>
	</div>

	<div>
		
		<span class="text-vit-text-muted">Gemuteter Text</span><br />
		<span class="text-vit-text-main">Normaler Text</span><br />
		<span class="text-vit-highlight">Highlight Text</span><br />
		<span class="text-vit-accent">Accent Text</span><br />
	</div>

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
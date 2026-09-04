<!-- eslint-disable vue/multi-word-component-names -->
<!-- src/components/base/Tooltip.vue -->
<script setup lang="ts">
import { ref } from 'vue'

const props = withDefaults(
  defineProps<{
    text: string
    align?: 'center' | 'left' | 'right'
  }>(), 
  {
    text: '',
    align: 'center' // Standardwert
  }
)

const show = ref(false)
</script>


<template>
  <div class="relative inline-block" @mouseenter="show = true" @mouseleave="show = false">
    <!-- Slot für den Button -->
    <slot />

    <!-- Tooltip-Box mit Animation -->
    <Transition
      enter-active-class="transition duration-150 ease-out"
      enter-from-class="transform scale-95 opacity-0"
      enter-to-class="transform scale-100 opacity-100"
      leave-active-class="transition duration-100 ease-in"
      leave-from-class="transform scale-100 opacity-100"
      leave-to-class="transform scale-95 opacity-0"
    >
      <div 
        v-if="show" 
        class="absolute left-4 top--4 z-100 mt-2 min-w-max rounded-vit-panel-radius bg-vit-bg p-2 text-md text-vit-text-main shadow-vit-shadow border border-vit-border"
		:class="{
          'left-1/2 -translate-x-1/2': props.align === 'center',  /* Standard */
          'left-0': props.align === 'left',                       /* Für Elemente am linken Bildschirmrand */
          'right-0': props.align === 'right'                      /* Für Elemente am rechten Bildschirmrand */
        }"
      >
        <!-- Slot für komplexen Inhalt (Fallback auf die text-Prop, falls leer) -->
 			<div class="flex items-center gap-2 max-w-xs">
          <div>
            <p>{{ props.text }}</p>
          </div>
        </div>
        
        <!-- Kleiner Pfeil unten -->
        <div class="absolute bottom-full left-1/2 -mb-1 h-2 w-2 -translate-x-1/2 rotate-45 bg-vit-bg border-l border-t border-vit-border"></div>
      </div>
    </Transition>
  </div>
</template>
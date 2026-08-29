<script setup lang="ts">

//import { ref } from 'vue'
import IconClose from '@/icons/IconClose.vue';
import type { ServerError } from '../types/vivien-generated'
import BaseButton from './base/BaseButton.vue';
import BaseIconButton from './base/BaseIconButton.vue';
import BasePanel from './base/BasePanel.vue';

// Erweitere das Interface für den lokalen UI-Zustand (Ausklappen)
interface UIError extends ServerError {
  _showDetails?: boolean;
}

// Erwarte das Fehler-Array aus der Hauptkomponente als Prop
const props = defineProps<{
  errors: UIError[]
}>()

const emit = defineEmits<{
  (e: 'dismissError', index: number): void
}>()

const removeError = (index: number) => {
  emit('dismissError', index)
}
</script>

<template>
  <!-- Container fixiert oben zentriert -->
  <div class="fixed top-6 left-1/2 -translate-x-1/2 z-50 w-full max-w-2xl px-4 flex flex-col gap-2">
    
    <!-- Animierter Übergang beim Löschen von Fehlern -->
    <TransitionGroup name="error-fade">
      <BasePanel 
        v-for="(err, index) in props.errors" 
        :key="index"
        variant="warning"
      >
        <!-- Kopfzeile des Fehlers -->
        <div class="flex items-start justify-between gap-3">
          <div class="flex items-center gap-3 p-2">
            <span class="flex-shrink-0 text-xl">⚠️</span>
            <div>
			  <p class="text-vit-text-muted text-xs">Backend-Fehler</p>
              <div class="font-semibold text-sm md:text-base text-vit-text-danger">
                {{ err.message }}
              </div>
              
            </div>
          </div>
          
          <!-- Buttons -->
          <div class="flex items-center gap-2">
            <!-- Details Umschalter -->
			 <BaseButton variant="normal"
              v-if="err.stacktrace"
              @click="err._showDetails = !err._showDetails"
            >
              {{ err._showDetails ? 'Details ausblenden' : 'Details zeigen' }}
            </BaseButton>
            
            <!-- Schließen Button -->
			 <BaseIconButton variant="normal"
			 	@click="removeError(index)"><IconClose class="text-vit-text-muted" /></BaseIconButton>
          </div>
        </div>

        <!-- Ausklappbarer Stacktrace -->
        <div 
          v-if="err.stacktrace && err._showDetails" 
          class="mt-3 pt-3 border-t border-vit-border/80"
        >
          <pre class="text-left text-xs font-mono text-red-300 bg-black/50 p-3 rounded-lg overflow-x-auto max-h-40 shadow-inner select-all">{{ err.stacktrace }}</pre>
        </div>

      </BasePanel>
    </TransitionGroup>
  </div>
</template>

<style scoped>
@import "tailwindcss";

.error-fade-enter-active {
  @apply transform ease-out duration-300 transition;
}

.error-fade-enter-from {
  @apply -translate-y-4 opacity-0;
}

.error-fade-enter-to {
  @apply translate-y-0 opacity-100;
}

.error-fade-leave-active {
  @apply transform ease-in duration-200 transition absolute w-full;
}

.error-fade-leave-from {
  @apply opacity-100;
}

.error-fade-leave-to {
  @apply opacity-0 scale-95;
}
</style>
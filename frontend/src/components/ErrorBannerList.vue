<script setup lang="ts">

//import { ref } from 'vue'
import type { ServerError } from '../types/vivien-generated'

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
  <div class="error-container">
    
    <!-- Animierter Übergang beim Löschen von Fehlern -->
    <TransitionGroup name="error-fade">
      <div 
        v-for="(err, index) in props.errors" 
        :key="index"
        class="error-card"
      >
        <!-- Kopfzeile des Fehlers -->
        <div class="error-header">
          <div class="error-content">
            <span class="error-icon">⚠️</span>
            <div>
              <h4 class="error-title">
                {{ err.message }}
              </h4>
              <p class="error-subtitle">Backend-Fehler aufgetreten</p>
            </div>
          </div>
          
          <!-- Buttons -->
          <div class="error-actions">
            <!-- Details Umschalter -->
            <button 
              v-if="err.stacktrace"
              @click="err._showDetails = !err._showDetails"
              class="btn-details"
            >
              {{ err._showDetails ? 'Details ausblenden' : 'Details zeigen' }}
            </button>
            
            <!-- Schließen Button -->
            <button 
              @click="removeError(index)" 
              class="btn-close"
            >
              &times;
            </button>
          </div>
        </div>

        <!-- Ausklappbarer Stacktrace -->
        <div 
          v-if="err.stacktrace && err._showDetails" 
          class="error-details"
        >
          <pre class="stacktrace-box">{{ err.stacktrace }}</pre>
        </div>

      </div>
    </TransitionGroup>
  </div>
</template>

<style scoped>
@import "tailwindcss";

/* 1. Layout-Strukturen */
.error-container {
  @apply fixed top-4 left-1/2 -translate-x-1/2 z-50 w-full max-w-2xl px-4 flex flex-col gap-2;
}

.error-card {
  @apply bg-slate-900 border border-red-500/40 rounded-xl p-4 shadow-2xl backdrop-blur-md;
}

.error-header {
  @apply flex items-start justify-between gap-3;
}

.error-content {
  @apply flex items-center gap-3;
}

.error-actions {
  @apply flex items-center gap-2;
}

/* 2. Textelemente */
.error-icon {
  @apply flex-shrink-0 text-xl;
}

.error-title {
  @apply font-semibold text-red-200 text-sm md:text-base;
}

.error-subtitle {
  @apply text-xs text-slate-400 mt-0.5;
}

/* 3. Interaktive Elemente / Buttons */
.btn-details {
  @apply text-xs bg-slate-800 hover:bg-slate-700 text-slate-300 px-2.5 py-1 rounded-md border border-slate-700 transition-colors;
}

.btn-close {
  @apply text-slate-400 hover:text-white text-lg p-1 leading-none rounded-md hover:bg-slate-800 transition-colors;
}

/* 4. Details / Stacktrace */
.error-details {
  @apply mt-3 pt-3 border-t border-slate-800/80;
}

.stacktrace-box {
  @apply text-left text-xs font-mono text-red-300 bg-black/50 p-3 rounded-lg overflow-x-auto max-h-40 shadow-inner select-all;
}

/* 5. Vue TransitionGroup Animationen (Das vierteilig-wüste Inline-CSS ersetzt durch natives Vue-CSS) */
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
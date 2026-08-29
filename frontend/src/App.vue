<!-- src/App.vue -->
<script setup lang="ts">

import { ref, onMounted } from 'vue'
import type { FileObject, RepositoryElement, ServerState } from './types/vivien-generated'
import ErrorBannerList from './components/ErrorBannerList.vue'
import TheHeader from './components/TheHeader.vue'
import TheSidebar from './components/TheSidebar.vue'
import RepoFileView from './components/views/RepoFileView.vue'
import BasePanel from './components/base/BasePanel.vue'
import ThePreviewPanel from './components/ThePreviewPanel.vue'
import { fetchWithView } from './client.ts'

const state = ref<ServerState>({
	user: undefined,
	view: 'Loading',
	mode: 'SETUP',
	serverErrors: []
})

const isSidebarOpen = ref(true)
const isLoading = ref<boolean>(true)
const networkError = ref<string | null>(null)
const previewImage = ref<FileObject | null>(null);

async function checkBackendStatus()
{
  try {
    isLoading.value = true
    networkError.value = null

    // Durch den Vite-Proxy wird dies an http://localhost:8080/api/state weitergeleitet
    const response = await fetchWithView("/api/state")
    
    if (!response.ok) {
      throw new Error(`Server antwortete mit Status: ${response.status}`)
    }

    // Daten reaktiv in den State schreiben
    state.value = await response.json()
  } catch (err: unknown) {
    console.error("Fehler beim API-Call:", err)
    networkError.value = "Vivien-Backend ist nicht erreichbar. Läuft der Java-Server?"
  } finally {
    isLoading.value = false
  }
}

async function updatePreview(el: RepositoryElement | null)
{
	// Schön informiert zu werden, aber ohne Objekt passiert nichts.
	if (el == null) return;

	const response = await fetchWithView(`/api/preview?file=${el.path}`);
	if (response.ok)
	{
		const fileObject: FileObject = await response.json();
		previewImage.value = fileObject;
	}
}

// Lifecycle-Hook: Wird ausgeführt, sobald die Komponente im Browser geladen ist
onMounted(() => {
  checkBackendStatus()
})

</script>

<template>
  <div class="h-screen w-screen flex flex-col overflow-hidden select-none">
    
    <TheHeader :state="state" />

    <!-- Inhalt unter dem Header -->
    <div class="flex flex-1 min-h-0">
      
      <!-- Unsere saubere Sidebar -->
      <TheSidebar :is-open="isSidebarOpen" />

      <!-- Hauptbereich -->
      <main class="flex-1 bg-vit-bg p-1 overflow-y-auto min-w-0">
        <ErrorBannerList 
          :errors="state.serverErrors" 
          @dismiss-error="(index) => state.serverErrors.splice(index, 1)"
        />

        <RepoFileView 
          @server-error="(err) => state.serverErrors.push(err)"
		  @select="(e) => updatePreview(e)"
        />
      </main>

	  <ThePreviewPanel :imageData="previewImage" />

	  <div class="fixed bottom-6 right-6 z-50 flex flex-row gap-4 max-w-2xl pointer-events-none">
	  <BasePanel variant="dialog" >Surface</BasePanel>
      <BasePanel variant="info" >Das ist ein Toast<br /><span class="text-vit-text-muted">Zweite Zeile.</span></BasePanel>
	  <BasePanel variant="warning" >Warning</BasePanel>
	  </div>

    </div>
  </div>
</template>

<style scoped></style>

<script setup lang="ts">

import { ref, onMounted } from 'vue'
import type { ServerState } from './types/vivien-generated'
import ErrorBannerList from './components/ErrorBannerList.vue'
import TheHeader from './components/TheHeader.vue'
import TheSidebar from './components/TheSidebar.vue'
import RepoFileView from './components/views/RepoFileView.vue' // Neu importiert

const state = ref<ServerState>({
	user: undefined,
	view: 'Loading',
	mode: 'SETUP',
	serverErrors: []
})

const isSidebarOpen = ref(true)
const isLoading = ref<boolean>(true)
const networkError = ref<string | null>(null)
//const currentPage = ref('dashboard')

const mockRepository = ref({
  elements: [
    {
      name: "🎨 Grafiken & UI",
      type: "FOLDER",
      childs: [
        {
          name: "Charaktere",
          type: "FOLDER",
          childs: [
            { name: "fuchs_held_walk_anim.png", type: "FILE", childs: [] },
            { name: "bösewicht_pose.png", type: "FILE", childs: [] },
            { name: "npc_händler_concept.jpg", type: "FILE", childs: [] }
          ]
        },
        {
          name: "Umgebung",
          type: "FOLDER",
          childs: [
            { name: "tileset_wald_frühling.png", type: "FILE", childs: [] },
            { name: "hintergrund_parallax_wolken.png", type: "FILE", childs: [] }
          ]
        },
        { name: "ui_hauptmenü_entwurf.png", type: "FILE", childs: [] },
        { name: "app_icon_final.ico", type: "FILE", childs: [] }
      ]
    },
    {
      name: "✍️ Story & Dialoge",
      type: "FOLDER",
      childs: [
        { name: "intro_sequenz_skript.txt", type: "FILE", childs: [] },
        { name: "charakter_backstories.md", type: "FILE", childs: [] },
        { name: "dialog_baum_händler_v2.json", type: "FILE", childs: [] }
      ]
    },
    {
      name: "🎵 Audio & Musik",
      type: "FOLDER",
      childs: [
        { name: "titelbildschirm_theme.wav", type: "FILE", childs: [] },
        { name: "soundeffect_schwert_hieb.mp3", type: "FILE", childs: [] },
        { name: "ambient_wald_vögel.ogg", type: "FILE", childs: [] }
      ]
    },
    {
      name: "⚙️ Balance & Design",
      type: "FOLDER",
      childs: [
        { name: "gegner_werte_tabelle.csv", type: "FILE", childs: [] },
        { name: "items_und_waffen.json", type: "FILE", childs: [] }
      ]
    },
    {
      name: "readme_anleitung_für_neulinge.md",
      type: "FILE",
      childs: []
    },
    {
      name: "project_settings.ini",
      type: "FILE",
      childs: []
    }
  ]
})

async function checkBackendStatus()
{
  try {
    isLoading.value = true
    networkError.value = null

    // Durch den Vite-Proxy wird dies an http://localhost:8080/api/state weitergeleitet
    const response = await fetch('/api/state')
    
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

// Lifecycle-Hook: Wird ausgeführt, sobald die Komponente im Browser geladen ist
onMounted(() => {
  checkBackendStatus()
})

</script>

<template>
  <div class="h-screen w-screen flex flex-col overflow-hidden select-none">
    
    <TheHeader :server-mode="state.mode" />

    <!-- Inhalt unter dem Header -->
    <div class="flex flex-1 min-h-0">
      
      <!-- Unsere saubere Sidebar -->
      <TheSidebar :is-open="isSidebarOpen" />

      <!-- Hauptbereich -->
      <main class="flex-1 bg-vit-bg p-4 overflow-y-auto min-w-0">
        <ErrorBannerList 
          :errors="state.serverErrors" 
          @dismiss-error="(index) => state.serverErrors.splice(index, 1)"
        />

        <RepoFileView 
            :repository="mockRepository" 
          />
      </main>

    </div>
  </div>
</template>

<style scoped></style>

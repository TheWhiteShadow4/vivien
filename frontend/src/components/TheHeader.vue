<script setup lang="ts">
import ViewTypePanel from '../components/ViewTypePanel.vue'
import { useStore } from '@/store';

const store = useStore();

const headerContainer = "h-16 w-full bg-vit-surface border-b border-vit-border px-6 flex items-center justify-between z-20 shrink-0"
</script>

<template>
  <header :class="headerContainer">
    <!-- Logo und Anwendungsname "Vivien" -->
    <div class="flex items-center gap-3">
		<img id="logo" src="/icon_192.png" width="48" height="48" alt="Logo" />
	    <h1 class="text-xl font-bold tracking-wide bg-linear-to-r from-purple-400 to-pink-400 bg-clip-text text-transparent">
          Vivien
		</h1>
    </div>
	<div id="fileview-toolbar" class="w-220 flex items-start mx-4 gap-6"></div>

    <ViewTypePanel v-if="store.server" :view="store.server.view" />
  </header>
</template>

<style scoped>
#logo {
	animation: drehen 30s linear infinite;
}

@keyframes drehen {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
<script setup lang="ts">
import IconVivien from '../icons/IconVivien.vue'
import ViewTypePanel from '../components/ViewTypePanel.vue'
import type { ServerState } from '@/types/vivien-generated.js';


const props = defineProps<{ state?: ServerState }>()

const headerContainer = "h-14 w-full bg-vit-surface border-b border-vit-border px-6 flex items-center justify-between z-20 shrink-0"
</script>

<template>
  <header :class="headerContainer">
    <!-- Logo und Anwendungsname "Vivien" -->
    <div class="flex items-center gap-3">
        <IconVivien />
	    <h1 class="text-xl font-bold tracking-wide bg-gradient-to-r from-purple-400 to-pink-400 bg-clip-text text-transparent">
          Vivien
		</h1>
    </div>

    <ViewTypePanel v-if="props.state" :view="props.state.view" />
  </header>
</template>
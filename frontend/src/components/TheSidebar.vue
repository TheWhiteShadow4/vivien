<!-- src/components/layout/TheSidebar.vue -->
<script setup lang="ts">
import type { ServerState } from '@/types/vivien-generated.js';
import GitControls from './views/GitControls.vue'
import UserInfo from './UserInfo.vue';
import BaseIconButton from './base/BaseIconButton.vue';
import IconArrow from '@/icons/IconArrow.vue';
import { computed } from 'vue';
import { useStore } from '@/store/index.ts';

const store = useStore();

const props = defineProps<{ state?: ServerState }>()

function toggleSidebar()
{
	store.updateSetting("sidebar", !store.settings.sidebar);
}

const sidebarContainer = `bg-vit-surface flex flex-col justify-start shrink-0
duration-100 ease-in overflow-hidden`
const sidebarMenu = "h-full border-r border-vit-border flex flex-col justify-start py-4"
const branchBadge = "border border-vit-accent px-4 py-2"
const toggleIcon = computed(() => store.settings.sidebar ? "rotate-90" : "rotate-270");
const headerLayoutStyles  = computed(() => {
  return store.settings.sidebar 
    ? 'flex flex-row-reverse justify-between items-center mr-2 py-2' //Offen
    : 'flex flex-col justify-center items-center py-2'    // Zu:
})
</script>

<template>
  <aside :class="[sidebarContainer, store.settings.sidebar ? 'w-56' : 'w-16']">
	<div :class="headerLayoutStyles" >
		<BaseIconButton @click="toggleSidebar()"><IconArrow :class="toggleIcon" /></BaseIconButton>
		<UserInfo v-if="props.state?.user" :small="!store.settings.sidebar" :user="props.state.user" />	
	</div>
	<div v-if="store.settings.sidebar" :class="branchBadge">
		<span class="pr-2 text-vit-text-muted">Branch:</span>
		<span class="font-bold">{{ store.git?.branch }}</span>
	</div>
	<div :class="sidebarMenu">
		<GitControls :variant="store.settings.sidebar ? 'full' : 'small'" />

	<!--<div class="flex flex-col gap-3">
        <h3 class="text-xs font-semibold text-vit-text-muted uppercase tracking-wider">Base Buttons</h3>
        
        <div class="grid grid-cols-2 gap-3">
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
	</div>-->
	
	</div>
  </aside>
</template>
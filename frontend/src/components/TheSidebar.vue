<!-- src/components/layout/TheSidebar.vue -->
<script setup lang="ts">
import GitControls from './views/GitControls.vue'
import UserInfo from './UserInfo.vue';
import BaseIconButton from './base/BaseIconButton.vue';
import IconArrow from '@/icons/IconArrow.vue';
import { computed } from 'vue';
import { useStore } from '@/store/index';
import ListButton from './base/ListButton.vue';
import IconHistory from '@/icons/IconHistory.vue';

const store = useStore();

const emit = defineEmits(["git", "user", "hist"]);

function toggleSidebar()
{
	store.updateSetting("sidebar", !store.settings.sidebar);
}

const sidebarContainer = `bg-vit-surface flex flex-col justify-start shrink-0
duration-100 ease-in overflow-hidden`;
const sidebarMenu = "h-full border-r border-vit-border flex flex-col justify-start";
const branchBadge = "border border-vit-accent px-4 py-2 mb-6";
const toggleIcon = computed(() => store.settings.sidebar ? "rotate-90" : "rotate-270");
const headerLayoutStyles  = computed(() => {
  return store.settings.sidebar 
    ? 'flex flex-row-reverse justify-between items-center mr-2' //Offen
    : 'flex flex-col justify-center items-center py-2'    // Zu:
})
</script>

<template>
  <aside :class="[sidebarContainer, store.settings.sidebar ? 'w-56' : 'w-16']">
	<div :class="headerLayoutStyles" >
		<BaseIconButton class="my-2" @click="toggleSidebar()"><IconArrow :class="toggleIcon" /></BaseIconButton>
		<UserInfo
			v-if="store.settings.username"
			:small="!store.settings.sidebar"
			:username="store.settings.username"
			:view="store.settings.view"
			@click="emit('user')"
		/>	
	</div>
	<div v-if="store.settings.sidebar" :class="branchBadge">
		<span class="pr-2 text-vit-text-muted">Branch:</span>
		<span class="font-bold">{{ store.git?.branch }}</span>
	</div>
	<div :class="sidebarMenu">
		<GitControls
			:variant="store.settings.sidebar ? 'full' : 'small'"
			@git="emit('git', $event)"
			/>

		<div>
			<ListButton
				color="normal"
				label="Historie"
				:minified="!store.settings.sidebar"
				@click="emit('hist')">
				<IconHistory />
			</ListButton>
		</div>
	</div>
  </aside>
</template>
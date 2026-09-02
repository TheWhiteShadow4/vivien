<!-- src/components/ViewTypePanel.vue -->
<script setup lang="ts">
import { useStore } from '@/store'
import { computed } from 'vue';

const store = useStore()
const view = computed(() => store.settings.view);

function toggleView()
{
	store.settings.view = store.settings.view == "admin" ? "artist" : "admin"; 
}
const badgeStyle = computed(() => {return{
	admin: "bg-vit-highlight/30 border border-vit-text-danger",
	artist: "bg-vit-accent/30 border border-vit-accent"
}[view.value]});

const badgeLabelStyle = computed(() => {return{
	admin: "text-vit-text-danger",
	artist: "text-vit-accent"
}[view.value]});

</script>

<template>
	<div class="flex items-center cursor-pointer" @click="toggleView">
		<div class="w-30 flex items-center gap-2 px-4 py-2 rounded-vit-panel-radius text-sm" :class="badgeStyle">
		<span class="text-vit-text-muted">View: <strong :class="badgeLabelStyle">{{ view.charAt(0).toUpperCase() + view.slice(1) }}</strong></span>
		</div>
	</div>
</template>
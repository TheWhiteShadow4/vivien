<!-- src/components/ViewTypePanel.vue -->
<script setup lang="ts">
import { updateUser } from '@/client';
import { useStore } from '@/store'
import type { UserRequest } from '@/types/vivien-generated';
import { computed, ref } from 'vue';

const store = useStore()
const view = computed(() => store.settings.view);

const isLoading = ref<boolean>(false);

async function toggleView()
{
	if (isLoading.value) return;

	const user = store.settings.username;
	if (!user) return;
	try
	{
		isLoading.value = true;

		const newView = store.settings.view == "admin" ? "artist" : "admin"; 
		if (await updateUser({ user: store.settings.username, view: newView } as UserRequest))
		{
			store.settings.view = newView;
		}
	}
	finally
	{
		isLoading.value = false;
	}
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
		<div class="w-32 flex items-center gap-2 px-4 py-2 rounded-vit-panel-radius text-md" :class="badgeStyle">
		<span class="text-vit-text-muted">View: <strong :class="badgeLabelStyle">{{ view.charAt(0).toUpperCase() + view.slice(1) }}</strong></span>
		</div>
	</div>
</template>
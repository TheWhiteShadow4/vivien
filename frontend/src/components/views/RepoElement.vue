<!-- src/components/base/BaseRepoElement.vue -->
<script setup lang="ts">
import type { RepositoryElement } from '@/types/vivien-generated'
import { computed } from 'vue'
import IconStar from '@/icons/IconStar.vue'
import { useStore } from '@/store/index'
import BaseIconButton from '@/base/BaseIconButton.vue';
import IconStarFilled from '@/icons/IconStarFilled.vue';

const store = useStore();

const props = defineProps<{
  element: RepositoryElement,
  label?: string,
  hint?: string,
  selected: boolean,
}>();

defineEmits(['clicked'])

// Visuelle Stile aus dem vit-Theme
const rowContainer = `w-full flex items-center justify-between p-3 h-14
border-b border-vit-border transition-colors cursor-pointer text-base`

const normalRowStyle = "bg-vit-surface hover:bg-vit-bg/50"
const selectedRowStyle = "bg-vit-accent-bg/30 hover:bg-vit-accent-bg/50"

const iconStyle = "text-xl select-none"
const nameStyle = "text-vit-text-main"
const normalStyle = "font-medium"
const selectedStyle = "font-bold"

// Typ-spezifische Farben und Icons
const preIcon = computed(() => {
	switch(props.element.type)
	{
		case 'VIRTUAL': return '🢨';
		case 'FOLDER': return '📁';
		case 'FILE': return '📄';
		case 'COMMIT': return '🔀';
		default: return '➤';
	}
})

const stateBadge = computed(() => {
	if (store.git)
	{
		if (store.git.added.indexOf(props.element.path) >= 0) return "bg-vit-accent";
		else if (store.git.changed.indexOf(props.element.path) >= 0) return "bg-vit-accent2";
		else if (store.git.modified.indexOf(props.element.path) >= 0) return "bg-vit-highlight";
		else if (store.git.untracked.indexOf(props.element.path) >= 0) return "bg-vit-highlight";
		else if (store.git.removed.indexOf(props.element.path) >= 0) return "bg-red-500";
		else if (store.git.missing.indexOf(props.element.path) >= 0) return "bg-red-500";
	}
	return "bg-vit-border";
})


function favorit(add: boolean)
{
	if (add)
	{
		store.addFavorite(props.element);
	}
	else
	{
		store.delFavorite(props.element);
	}
}

</script>

<template>
	<div :class="[rowContainer, selected ? selectedRowStyle : normalRowStyle]" @dblclick="$emit('clicked', true)"
		@click="$emit('clicked', false)">
		<!-- Linke Seite: Icon & Name -->
		<div class="flex items-center gap-4">
			<!-- Visuelles Feedback für den Typ -->
			<span :class="iconStyle">{{ preIcon }}</span>

			<span :class="[nameStyle, selected ? selectedStyle : normalStyle]">{{ label ? label : element.name }}</span>
			<span v-if="hint" class="text-vit-text-muted">({{ hint }})</span>
		</div>

		<div class="flex items-center text-vit-text-main">
			<template v-if="selected">
				<BaseIconButton v-if="store.settings.favorites.includes(element)" class="mx-2"
					@click.stop="favorit(false)">
					<IconStarFilled />
				</BaseIconButton>
				<BaseIconButton v-else class="mx-2" @click.stop="favorit(true)">
					<IconStar />
				</BaseIconButton>
			</template>

			<!-- Rechte Seite: Typ-Badge -->
			<span class="w-3 h-3 ml-4 rounded-full flex" :class="stateBadge"></span>
		</div>

	</div>
</template>
<!-- src/components/base/BaseRepoElement.vue -->
<script setup lang="ts">
import IconDownload from '@/icons/IconDownload.vue'
import { computed } from 'vue'
import BaseIconButton from './BaseIconButton.vue'
import IconStar from '@/icons/IconStar.vue'
import IconClose from '@/icons/IconClose.vue'

const props = defineProps({
  name: { type: String, required: true },
  type: { type: String, required: true },
  selected: { type: Boolean, required: true }
})

defineEmits(['clicked'])

// Visuelle Stile aus dem vit-Theme
const rowContainer = `w-full flex items-center justify-between p-3
border-b border-vit-border transition-colors cursor-pointer text-base`

const normalRowStyle = "bg-vit-surface hover:bg-vit-bg/50"
const selectedRowStyle = "bg-vit-accent-bg/30"

const iconStyle = "text-xl select-none"
const nameStyle = "text-vit-text-main"
const normalStyle = "font-medium"
const selectedStyle = "font-bold"

// Typ-spezifische Farben und Icons
const isFolder = computed(() => props.type === 'FOLDER')
const typeLabel = computed(() => isFolder.value ? 'Ordner' : 'Datei')
const typeBadge = computed(() => {
  return isFolder.value 
    ? "bg-vit-secondary/10 text-vit-secondary border border-vit-secondary/20"
    : "bg-vit-primary/10 text-vit-primary border border-vit-primary/20"
})


function download()
{
	
}

function favorit()
{

}

function deleteEntry()
{

}

</script>

<template>
  <div :class="[rowContainer, selected ? selectedRowStyle : normalRowStyle]" @dblclick="$emit('clicked', true)" @click="$emit('clicked', false)">
    <!-- Linke Seite: Icon & Name -->
    <div class="flex items-center gap-4">
      <!-- Visuelles Feedback für den Typ -->
      <span :class="iconStyle">
        {{ isFolder ? '📁' : '📄' }}
      </span>
      
      <span :class="[nameStyle, selected ? selectedStyle : normalStyle]">{{ name }}</span>
    </div>

	<div class="flex items-right text-vit-text-main">
		<template v-if="selected">
			<BaseIconButton v-if="type == 'FILE'" class="mx-2" @click.stop="download()"><IconDownload/></BaseIconButton>
			<BaseIconButton class="mx-2" @click.stop="favorit()"><IconStar/></BaseIconButton>
			<BaseIconButton class="mx-2" @click.stop="deleteEntry()"><IconClose/></BaseIconButton>
		</template>
		
		<!-- Rechte Seite: Typ-Badge -->
		<span class="px-3 py-1 ml-8 rounded-vit-radius w-20 text-xs font-mono" :class="typeBadge">
		{{ typeLabel }}
		</span>
	</div>

    

  </div>
</template>
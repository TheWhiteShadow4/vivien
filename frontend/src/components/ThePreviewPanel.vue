<!-- src\components\ThePreviewPanel.vue -->
<script setup lang="ts">
import BaseButton from './base/BaseButton.vue'

defineProps({
  activeView: String
})

const previewContainer = "w-2/5 bg-vit-surface border-l border-vit-border flex flex-col justify-between shrink-0 p-4"
</script>

<template>
	<section :class="previewContainer">

	</section>
</template>

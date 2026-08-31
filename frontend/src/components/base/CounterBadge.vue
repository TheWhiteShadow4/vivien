<!-- src/components/base/CounterBadge.vue -->
<script setup lang="ts">

const props = defineProps({
  count: { type: Number, required: true }
});

</script>

<template>
	<div class="w-5 h-5 m-2 border border-vit-border bg-vit-bg rounded-full flex flex-col items-center">
		<span class="inline-flex items-center h-5 text-sm font-bold font-mono text-vit-primary2">{{ props.count }}</span>
	</div>
</template>
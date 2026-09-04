<!-- eslint-disable vue/multi-word-component-names -->
<!-- src/components/base/Splitter.vue -->
<script setup lang="ts">
import { Pane, Splitpanes } from 'splitpanes'
import 'splitpanes/dist/splitpanes.css'

</script>

<template>
<Splitpanes class="h-full" vertical>
	<Pane min-size="20">
		<slot name="links" />
	</pane>
	<pane min-size="20">
		<slot name="rechts" />
	</pane>
</splitpanes>
</template>

<style>
.splitpanes--vertical > .splitpanes__splitter
{
	width: 10px; /* Erhöht die Klickfläche auf 10px */
	background-color: var(--color-vit-border); /* Sichtbare Farbe für den Splitter */
}
.splitpanes--vertical > .splitpanes__splitter:active
{
	background-color: var(--color-vit-highlight);
}

.splitpanes--dragging {
  user-select: none;
  -webkit-user-select: none;
}
</style>
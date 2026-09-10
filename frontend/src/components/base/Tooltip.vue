<!-- eslint-disable vue/multi-word-component-names -->
<!-- src/components/base/Tooltip.vue -->
<script setup lang="ts">
import { computed, onUnmounted, ref } from 'vue'

const coords = ref({ top: 0, left: 0 })

const props = withDefaults(
	defineProps<{
		text: string
		align?: 'center' | 'left' | 'right'
	}>(),
	{
		text: '',
		align: 'center' // Standardwert
	}
)

let timeoutId: ReturnType<typeof setTimeout> | null = null
const show = ref(false);

function showTooltip(evt: MouseEvent)
{
	if (timeoutId) clearTimeout(timeoutId);

	const x = evt.pageX;
	const y = evt.pageY;
	timeoutId = setTimeout(() => {
		coords.value.left = x;
		coords.value.top = y;
		show.value = true;
	}, 300);
}

function hideTooltip()
{
	if (timeoutId)
	{
		clearTimeout(timeoutId)
		timeoutId = null
	}
	show.value = false;
}

onUnmounted(() => {
  if (timeoutId) clearTimeout(timeoutId)
})

const baseStyle = "absolute z-100 mt-2 min-w-max rounded-vit-panel-radius bg-vit-bg p-2 text-md text-vit-text-main shadow-vit-shadow border border-vit-border";
const layoutStyle = computed(() => {return{
	'left-1/2 -translate-x-1/2': props.align === 'center',  /* Standard */
	'left-0': props.align === 'left',                       /* Für Elemente am linken Bildschirmrand */
	'right-0': props.align === 'right'                      /* Für Elemente am rechten Bildschirmrand */
}});
const positionStyle = computed(() => {
	return {top: `${coords.value.top}px`, left: `${coords.value.left}px`}
});
</script>

<template>
	<div class="relative inline-block" @mouseenter="showTooltip($event)" @mouseleave="hideTooltip">
		<slot />

		<!-- Tooltip-Box mit Animation -->
		<Transition enter-active-class="transition duration-150 ease-out"
			enter-from-class="transform scale-95 opacity-0" enter-to-class="transform scale-100 opacity-100"
			leave-active-class="transition duration-100 ease-in" leave-from-class="transform scale-100 opacity-100"
			leave-to-class="transform scale-95 opacity-0">
			<Teleport v-if="show" to="body">
				<div :class="[baseStyle, layoutStyle]" :style="positionStyle">
					<!-- Slot für komplexen Inhalt (Fallback auf die text-Prop, falls leer) -->
					<div class="flex items-center gap-2 max-w-xs">
						<div>
							<p>{{ props.text }}</p>
						</div>
					</div>

					<div
						class="absolute bottom-full left-1/2 -mb-1 h-2 w-2 -translate-x-1/2 rotate-45 bg-vit-bg border-l border-t border-vit-border">
					</div>
				</div>
			</Teleport>
		</Transition>
	</div>
</template>
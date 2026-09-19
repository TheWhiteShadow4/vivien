<!-- src\components\AudioPlayer.vue -->
<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import WaveSurfer from 'wavesurfer.js'
import BaseIconButton from '@/base/BaseIconButton.vue';
import IconPause from '@/icons/IconPause.vue';
import IconPlay from '@/icons/IconPlay.vue';

const props = defineProps<{
	url: string
}>()


const containerRef = ref<HTMLElement | null>(null)
const ws = ref<WaveSurfer | null>(null)

const isPlaying = ref(false)

const initPlayer = async () => {
	if (!containerRef.value) return

	if (ws.value)
	{
		ws.value.destroy()
	}

	try
	{
		ws.value = WaveSurfer.create({
			container: containerRef.value,
			waveColor: '#7b73a5',
			progressColor: '#00cc99',
			cursorColor: '#ffffff',
			height: 64,
			normalize: true,
			backend: 'WebAudio'
		})

		// Events binden
		ws.value.on('play', () => isPlaying.value = true)
		ws.value.on('pause', () => isPlaying.value = false)

		// Stream-URL laden
		ws.value.load(props.url)
	}
	catch (error)
	{
		console.error('Fehler beim Initialisieren von Wavesurfer:', error)
	}
}

watch(() => props.url, () => { initPlayer() })
onMounted(() => { initPlayer() })
onBeforeUnmount(() => { if (ws.value) ws.value.destroy() })
</script>

<template>
	<div class="h-48 p-2">
		<div class="relative">
			<div ref="containerRef" class="w-full"></div>
			<BaseIconButton
				class="absolute top-2 left-1/2 z-100"
				size="lg"
				variant="primary"
				@click="ws?.playPause()">
				<IconPause v-if="isPlaying" />
				<IconPlay v-else />
			</BaseIconButton>
		</div>
	</div>
</template>
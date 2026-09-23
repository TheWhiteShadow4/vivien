<!-- src\views\MeshView.vue -->
<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue';
import type { FileObject } from '@/types/vivien-generated';
import { vue3dLoader } from 'vue-3d-loader'
import * as THREE from 'three'

const props = defineProps<{
	fileObject: FileObject
}>()

const containerRef = ref<HTMLDivElement | null>(null)

const dimensions = reactive({
	width: 0,
	height: 0
})

let resizeObserver: ResizeObserver | null = null

const mtl = computed(() => {
	return props.fileObject.metadata.additional?.split(',')
		.filter((p) => p.startsWith("mtl:"))
		.map((p) => p.substring(4)) ?? [];
});

onMounted(() => {
	if (containerRef.value)
	{
		resizeObserver = new ResizeObserver((entries) => {
			for (const entry of entries)
			{
				dimensions.width = entry.contentRect.width
				dimensions.height = entry.contentRect.height
			}
		})
		resizeObserver.observe(containerRef.value)
	}
})

onUnmounted(() => {
	if (resizeObserver && containerRef.value)
	{
		resizeObserver.unobserve(containerRef.value)
	}
})

const onModelLoaded = (scene: THREE.Scene) => {
	if (props.fileObject.url.endsWith("glb") || props.fileObject.url.endsWith("gltf"))
	{
		scene.traverse((child: any) => {
			if (child.isMesh)
			{
				// Falls das Modell mehrere Materialien hat, in ein Array vereinheitlichen
				const materials = Array.isArray(child.material) ? child.material : [child.material]
				materials.forEach((material: any) => {
					if (material)
					{
						material.transparent = false;

						material.depthWrite = true;
						material.depthTest = true;
						material.side = THREE.FrontSide

						if (material.alphaMode)
							material.alphaMode = 'OPAQUE'

						material.needsUpdate = true
					}
				})
			}
		})
	}
}

const customLights = [
	{
		type: 'AmbientLight',
		color: '#ffffff',
		intensity: 0.7
	},
	{
		type: 'DirectionalLight',
		color: '#ffffff',
		intensity: 1.2,
		position: { x: 50, y: 100, z: 50 } 
	}
]
</script>

<template>
	<div ref="containerRef" class="w-full h-full">
		<vue3dLoader
			:key="fileObject.url" 
			:filePath="fileObject.url"
			:mtlPath="mtl"
			:backgroundColor="0x2e2a42"
			:lighting="true"
			:lights="customLights"
			:autoPlay="false"
			:width="dimensions.width"
			:height="dimensions.height"
			@load="onModelLoaded"
			/>
	</div>
</template>
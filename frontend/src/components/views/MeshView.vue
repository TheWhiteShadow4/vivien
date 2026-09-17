<!-- src\views\MeshView.vue -->
<script setup lang="ts">
import { onMounted, onUnmounted, reactive, ref } from 'vue';
import { vue3dLoader } from 'vue-3d-loader'
import * as THREE from 'three'

const props = defineProps<{
	content: string
}>()

const containerRef = ref<HTMLDivElement | null>(null)

const dimensions = reactive({
	width: 0,
	height: 0
})

let resizeObserver: ResizeObserver | null = null

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

// Wichtig: Observer aufräumen, um Memory Leaks zu verhindern
onUnmounted(() => {
	if (resizeObserver && containerRef.value)
	{
		resizeObserver.unobserve(containerRef.value)
	}
})

const onModelLoaded = (scene: THREE.Scene) => {
  // 1. Durchlaufe alle Unterobjekte des geladenen GLB-Modells
  scene.traverse((child: any) => {
    // Prüfen, ob es sich um ein renderbares 3D-Mesh handelt
    if (child.isMesh) {
      
      // Falls das Modell mehrere Materialien hat, in ein Array vereinheitlichen
      const materials = Array.isArray(child.material) ? child.material : [child.material]
      
      materials.forEach((material: any) => {
        if (material) {
          // FEHLERBEHEBUNG 1: Transparenz-Sortierung deaktivieren (falls nicht explizit Glas/Fenster)
          // Viele Exporter setzen dies fälschlicherweise auf true
          material.transparent = false 
          
          // FEHLERBEHEBUNG 2: Tiefenpuffer erzwingen
          // Das sorgt dafür, dass Vordergrund-Objekte den Hintergrund korrekt verdecken (Occlusion)
          material.depthWrite = true
          material.depthTest = true
          
          // FEHLERBEHEBUNG 3: Backface Culling aktivieren
          // THREE.FrontSide sorgt dafür, dass Innenseiten/Rückseiten ausgeblendet werden
          material.side = THREE.FrontSide 
          
          // Optionale Absicherung für glTF-Sonderfälle (Alpha-Cutoff zurücksetzen)
          if (material.alphaMode) {
            material.alphaMode = 'OPAQUE'
          }
          
          // Drei.js anweisen, das Material mit den neuen Parametern zu aktualisieren
          material.needsUpdate = true
        }
      })
    }
  })
}

const customLights = [
  {
    type: 'AmbientLight',
    color: '#ffffff',
    intensity: 0.7 // Sorgt dafür, dass Schattenseiten nicht komplett schwarz absaufen
  },
  {
    type: 'DirectionalLight',
    color: '#ffffff',
    intensity: 1.2, // Höhere Intensität für klare Ausleuchtung
    // Position weit oben (y: 100) und leicht versetzt (x: 50, z: 50),
    // damit Kanten gut sichtbar sind (3D-Effekt)
    position: { x: 50, y: 100, z: 50 } 
  }
]
</script>

<template>
	<div ref="containerRef" class="w-full h-full">
		<vue3dLoader
			:key="content" 
			:filePath="content"
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
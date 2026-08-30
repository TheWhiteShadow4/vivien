<!-- src\components\views\MarkdownView.vue -->
<script setup lang="ts">
import { computed } from 'vue'
import { marked } from 'marked'
import DOMPurify from 'dompurify'

interface Props {
  content: string
}

const props = defineProps<Props>()

// Wandelt Markdown sicher in HTML um
const parsedHtml = computed(() => {
  if (!props.content) return ''
  
  // Konfiguriere marked (z.B. für Zeilenumbrüche)
  const rawHtml = marked.parse(props.content, { 
    breaks: true, 
    gfm: true 
  }) as string

  // Schutz vor Schadcode (XSS), falls jemand böswilligen HTML-Code in die MD einschleust
  return DOMPurify.sanitize(rawHtml)
})

// REGLER 5: Strukturelles Layout (Padding, Scroller)
const containerStyles = 'w-full h-full overflow-y-auto p-4 bg-vit-surface border border-vit-border shadow-vit-inset'

// REGLER 1 & 2: Wir stylen die HTML-Tags des Parsers über CSS-Variablen deines Themes
// Das hält das HTML-Template komplett frei von Klassen-Ketten
const markdownStyles = [
  'prose prose-invert max-w-none text-base font-sans text-vit-text-main',
  // Überschriften stylen
  '[&_h1]:text-xl [&_h1]:font-bold [&_h1]:text-vit-text-main [&_h1]:mb-4 [&_h1]:mt-2 [&_h1]:border-b [&_h1]:border-vit-border [&_h1]:pb-2',
  '[&_h2]:text-lg [&_h2]:font-semibold [&_h2]:text-vit-text-main [&_h2]:mb-3 [&_h2]:mt-4',
  '[&_h3]:text-base [&_h3]:font-medium [&_h3]:text-vit-highlight [&_h3]:mb-2 [&_h3]:mt-3',
  // Absätze und Listen
  '[&_p]:mb-4 [&_p]:leading-relaxed',
  '[&_ul]:list-disc [&_ul]:pl-6 [&_ul]:mb-4 [&_ul]:space-y-1',
  '[&_ol]:list-decimal [&_ol]:pl-6 [&_ol]:mb-4 [&_ol]:space-y-1',
  // Links und Code-Blöcke
  '[&_a]:text-vit-secondary [&_a]:underline hover:[&_a]:text-vit-accent transition-colors',
  '[&_code]:bg-vit-btn [&_code]:px-1.5 [&_code]:py-0.5 [&_code]:rounded [&_code]:text-sm [&_code]:font-mono [&_code]:border [&_code]:border-vit-border',
  '[&_pre]:bg-vit-bg [&_pre]:p-4 [&_pre]:rounded-vit-panel-radius [&_pre]:overflow-x-auto [&_pre]:font-mono [&_pre]:border [&_pre]:border-vit-border [&_pre]:mb-4',
  '[&_pre_code]:bg-transparent [&_pre_code]:p-0 [&_pre_code]:border-none text-vit-highlight',
  // Zitate
  '[&_blockquote]:border-l-4 [&_blockquote]:border-vit-primary [&_blockquote]:pl-4 [&_blockquote]:italic [&_blockquote]:text-vit-text-muted [&_blockquote]:my-4'
].join(' ')
</script>

<template>
	<div :class="containerStyles">
		<!-- Das generierte HTML wird über v-html sicher injiziert -->
		<div v-html="parsedHtml" :class="markdownStyles" />
	</div>
</template>
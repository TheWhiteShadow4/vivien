<!-- eslint-disable vue/no-parsing-error -->
<!-- src/components/icons/IconVivien.vue -->
<template>
  <svg 
    xmlns="http://w3.org" 
    viewBox="0 0 24 24" 
    fill="none" 
    class="w-7 h-7"
  >
    <defs>
      <linearGradient id="vitGradient" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" stop-color="var(--color-vit-primary)" />
        <stop offset="100%" stop-color="var(--color-vit-secondary)" />
      </linearGradient>
    </defs>

    <g 
      stroke="url(#vitGradient)" 
      stroke-width="1.5" 
      stroke-linecap="round" 
      stroke-linejoin="round"
    >
      <!-- RAUTE 1: Das untere Gesicht (Kinn & fluffige Wangen) -->
      <!-- Startet links außen am Zacken (3,11), geht zur Nasenspitze (12,21), zum rechten Zacken (21,11) und schließt oben flach (12,11) -->
      <path d="M3 11L12 21L21 11L12 11Z" />
      
      <!-- RAUTE 2: Die obere Kopfpartie mit Ohren (leicht nach oben/hinten verschoben) -->
      <!-- Startet etwas weiter innen (5,11), geht hoch zu den Ohrenspitzen und schließt unten ab -->
      <path d="M5 11L5 4L12 9L19 4L19 11Z" />
      
      <!-- DIE AUGEN: Völlig frei im oberen Bereich, keine störenden Linien mehr im Weg -->
      <circle cx="8.5" cy="14" r="0.75" fill="url(#vitGradient)" stroke="none" />
      <circle cx="15.5" cy="14" r="0.75" fill="url(#vitGradient)" stroke="none" />
    </g>
  </svg>
</template>
// src/client.ts
import { useStore } from '@/store'

/**
 * Ein Wrapper um das native fetch, der automatisch den View Parameter als Header mitsendet. 
 */
export async function fetchWithView(url: string, options: RequestInit = {}): Promise<Response>
{
  // 1. Store innerhalb der Funktion aufrufen (wichtig, da Pinia beim App-Start bereit sein muss)
  const store = useStore()

  // 2. Bestehende Header beibehalten oder neue Headers-Instanz erstellen
  const headers = new Headers(options.headers)

  // 3. Variablen aus dem Store als Custom-Header injizieren
  headers.set('X-App-View', store.settings.view)
  //headers.set('X-App-User', store.settings.username)
  
  // Falls du JSON sendest, kannst du das hier auch direkt als Standard setzen:
  if (!headers.has('Content-Type') && (options.method === 'POST' || options.method === 'PUT')) {
    headers.set('Content-Type', 'application/json')
  }

  // 4. Das originale fetch mit den erweiterten Optionen ausführen
  return fetch(url, {
    ...options,
    headers
  })
}
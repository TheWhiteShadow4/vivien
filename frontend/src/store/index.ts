// src\store\index.ts
import { ref, watch } from 'vue'
import { defineStore } from 'pinia'
import type { RepositoryElement, GitBranchStatus, ServerState } from '@/types/vivien-generated';

export interface UserSettings
{
	username?: string;
	email?: string;
	credentials?: string;
	view: string;
	sidebar: boolean;
	favorites: RepositoryElement[];
}

export const ALLOWED_EDITOR_TYPES = ["text/json", "text/yaml", "text/toml", "text/markdown"] as const;
export type EditorTypes = typeof ALLOWED_EDITOR_TYPES[number];

export const ALLOWED_MODEL_TYPES = ["application/fbx", "application/gltf", "application/glb", "application/obj"] as const;
export type ModelType = typeof ALLOWED_MODEL_TYPES[number];

export const ALLOWED_AUDIO_TYPES = ["audio/wav", "audio/mpeg", "audio/ogg", "audio/aac"] as const;
export type AudioType = typeof ALLOWED_AUDIO_TYPES[number];

export interface EditorFile
{
	path: string,
	content: string
	type: EditorTypes
	readOnly: boolean
	isDirty: boolean
}

// Der Name 'settings' ist der eindeutige Identifier des Stores
export const useStore = defineStore('settings', () => {

	const server = ref<ServerState | null>(null);
	const git = ref<GitBranchStatus>();
	const clipboard = ref<RepositoryElement | null>(null);
	const folder = ref<RepositoryElement | null>(null);
	const editor = ref<EditorFile | null>(null);

	// 1. Initialisierung: Versuche aus dem LocalStorage zu laden, sonst nimm Defaults
	const settings = ref<UserSettings>({
		view: 'artist',
		sidebar: true,
		favorites: [],
		...JSON.parse(localStorage.getItem('vivian_user') || '{}')
	})

	// 2. Aktion (Methode) zum Ändern einzelner Werte
	function updateSetting<K extends keyof UserSettings>(key: K, value: UserSettings[K]) {
		settings.value[key] = value
	}

	function addFavorite(el: RepositoryElement) {
		settings.value.favorites.push(el);
	}

	function delFavorite(el: RepositoryElement) {
		const index = settings.value.favorites.indexOf(el);
		if (index > -1) settings.value.favorites.splice(index, 1);
	}

	// 3. Watcher: Jedes Mal, wenn sich ein Wert im Objekt ändert, in LocalStorage schreiben
	watch(
		settings,
		(newSettings) => {
			localStorage.setItem('vivian_user', JSON.stringify(newSettings))
		},
		{ deep: true } // Wichtig bei Objekten, um Änderungen tief im Inneren zu bemerken
	)

	// Alles zurückgeben, was in Komponenten/Dateien verfügbar sein soll
	return {
		server,
		git,
		clipboard,
		folder,
		settings,
		editor,
		addFavorite,
		delFavorite,
		updateSetting
	}
})

export type StoreType = ReturnType<typeof useStore>;

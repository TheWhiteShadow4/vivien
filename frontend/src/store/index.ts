// src\store\index.ts
import { ref, watch } from 'vue'
import { defineStore } from 'pinia'
import type { GitBranchStatus, StageInfo } from '@/types/vivien-generated'

export interface UserSettings
{
	username?: string;
	email?: string;
	credentials?: string;
	view: string;
	sidebar: boolean;
}

export type EditorTypes = "text/json" | "text/yaml";

export interface EditorFile
{
	path: string,
	content: string
	type: EditorTypes
	isDirty: boolean
}

// Der Name 'settings' ist der eindeutige Identifier des Stores
export const useStore = defineStore('settings', () => {

	const git = ref<GitBranchStatus>();
	const stage = ref<StageInfo>();

	// 1. Initialisierung: Versuche aus dem LocalStorage zu laden, sonst nimm Defaults
	const settings = ref<UserSettings>({
		view: 'artist',
		sidebar: true,
		...JSON.parse(localStorage.getItem('vivian_user') || '{}')
	})

	/*function updateGit(value: GitBranchStatus) {
		git.value = value
	}*/

	// 2. Aktion (Methode) zum Ändern einzelner Werte
	function updateSetting<K extends keyof UserSettings>(key: K, value: UserSettings[K]) {
		settings.value[key] = value
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
		git,
		stage,
		settings,
		updateSetting
	}
})

export type StoreType = ReturnType<typeof useStore>;


export const useEditorStore = defineStore('codemirror', {
	state: () => ({
		openFiles: {} as Record<string, EditorFile>,
		activePath: null as string | null,
	}),
	getters: {
		activeFile: (state) => state.activePath ? state.openFiles[state.activePath] : null
	},
	actions: {
		openFile(path: string, content: string, type: EditorTypes): EditorFile
		{
			// Prüfen, ob die Datei bereits geöffnet ist
			const existing = !!this.openFiles[path];

			if (!existing)
			{
				this.openFiles[path] = {
					path,
					content,
					type,
					isDirty: false
				};
			}
			// Die Datei als aktiv markieren
			this.activePath = path;
			return this.openFiles[path]!;
		},
		updateContent(path: string, newContent: string) {
			const file = this.openFiles[path];
			if (file)
			{
				file.content = newContent;
			}
		}
	}
})
// src/client.ts
import { useStore } from '@/store'
import emitter from './mitt';
import type { CommitRequest, GitStageOperation, GitStageRequest, ServerError, StageInfo } from './types/vivien-generated';

/**
 * Ein Wrapper um das native fetch, der automatisch den View Parameter als Header mitsendet. 
 */
export async function fetchWithView(url: string, options: RequestInit = {}): Promise<Response>
{
	// 1. Store innerhalb der Funktion aufrufen (wichtig, da Pinia beim App-Start bereit sein muss)
	const store = useStore();

	// 2. Bestehende Header beibehalten oder neue Headers-Instanz erstellen
	const headers = new Headers(options.headers);

	// 3. Variablen aus dem Store als Custom-Header injizieren
	headers.set('X-App-View', store.settings.view);
	//headers.set('X-App-User', store.settings.username)

	// Falls du JSON sendest, kannst du das hier auch direkt als Standard setzen:
	if (!headers.has('Content-Type') && (options.method === 'POST' || options.method === 'PUT'))
	{
		headers.set('Content-Type', 'application/json');
	}

	// 4. Das originale fetch mit den erweiterten Optionen ausführen
	return fetch(url, {
		...options,
		headers
	});
}

export async function sendCommit(message: string): Promise<Response>
{
	const store = useStore();

	const options: RequestInit = {
		method: "POST",
		headers: {'Content-Type': 'application/json'},
		body: JSON.stringify({
			name: store.settings.username,
			email: store.settings.email,
			message
		} as CommitRequest)
	};
	return fetch("/api/commit", options);
}

export async function sendChangeStaged(file: string, op: GitStageOperation): Promise<Response>
{
	const store = useStore();

	const options: RequestInit = {
		method: "POST",
		headers: {'Content-Type': 'application/json'},
		body: JSON.stringify({
			op: op,
			email: store.settings.email,
			file: file
		} as GitStageRequest)
	};
	return fetch("/api/staged", options);
}

export function emitDisconectError(status: string)
{
	const message = `Server konnte nicht erreicht werden: ${status}`;
	console.error(message);
	emitter.emit('error', { message });
}

export async function checkGitStatus()
{
	const store = useStore();
	try
	{
		const response = await fetchWithView("/api/git")

		if (!response.ok) {
			emitDisconectError(response.statusText);
			return;
		}

		store.git = await response.json();
	}
	catch (err: unknown)
	{
		console.log(err);
	}
}

export async function uploadFiles(event: Event, fileOrFolder: string): Promise<boolean>
{
	const store = useStore();

	if (!store.settings.email)
	{
		emitter.emit("errror", { message: "Email nicht gesetzt."} as ServerError)
		return false;
	}

	const target = event.target as HTMLInputElement;
	if (target.files && target.files.length > 0)
	{
		const formData = new FormData()

		formData.append('email', store.settings.email);
		formData.append('fileOrFolder', fileOrFolder);

		Array.from(target.files).forEach((file) => {
			formData.append('files', file)
		})

		try
		{
			const response = await fetch('/api/upload', {
				method: 'POST',
				body: formData,
			})

			if (response.ok)
			{
				store.stage = await response.json() as StageInfo
				emitter.emit("refresh-folder");
				return true;
			}
			else
			{
				const error = await response.json() as ServerError
				emitter.emit("error", error);
			}
		} catch (error) {
			console.error('Netzwerkfehler beim Upload:', error)
		}
	}
	return false;
}
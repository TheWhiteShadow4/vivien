// src/client.ts
import { useStore } from '@/store'
import emitter from './mitt';
import type { GitBranchStatus, GitStageOperation, GitStageRequest, ServerError, StageInfo } from './types/vivien-generated';


export async function fetchWithView(url: string, options: RequestInit = {}): Promise<Response>
{
	const store = useStore();

	const headers = new Headers(options.headers);

	headers.set('X-App-View', store.settings.view);
	headers.set('Authorization', `Basic ${store.settings.credentials}`);

	if (!headers.has('Content-Type') && (options.method === 'POST'))
	{
		headers.set('Content-Type', 'application/json');
	}

	return fetch(url, {
		...options,
		headers
	});
}

export async function sendChangeStaged(file: string, op: GitStageOperation): Promise<Response>
{
	const store = useStore();

	const options: RequestInit = {
		method: "POST",
		body: JSON.stringify({
			op: op,
			email: store.settings.email,
			file: file
		} as GitStageRequest)
	};
	return fetchWithView("/api/staged", options);
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
				headers: {'Authorization': `Basic ${store.settings.credentials}`},
				method: 'POST',
				body: formData,
			})

			if (response.ok)
			{
				store.git = await response.json() as GitBranchStatus
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
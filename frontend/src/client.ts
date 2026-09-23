// src/client.ts
import { useStore } from '@/store'
import emitter from './mitt';
import type { FileObject, GitBranchStatus, GitStageOperation, GitStageRequest, LoginRequest, LoginResult, PluginRequest, RepositoryElement, ServerError } from './types/vivien-generated';
import { getFileExtension, getFilename } from '@/config';


export async function fetchWithView(url: string, options: RequestInit = {}): Promise<Response>
{
	const store = useStore();

	const headers = new Headers(options.headers);

	// Der View wird momentan immer gesendet
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

export async function sendLogin(path: string): Promise<LoginResult | null>
{
	const store = useStore();
	if (!store.settings?.credentials) return null;

	const [user, pass] = atob(store.settings.credentials)?.split(':');
	const options: RequestInit = {
		method: "POST",
		body: JSON.stringify({
			user: user,
			pass: pass,
			view: store.settings.view,
			path: path
		} as LoginRequest)
	};
	const resp = await fetch("/api/login", options);
	if (resp.ok)
	{
		return await resp.json();
	}
	return null;
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

export async function sendUploadRequest(formData: FormData): Promise<boolean>
{
	const store = useStore();

	try
	{
		const response = await fetch('/api/upload', {
			headers: {
				'Authorization': `Basic ${store.settings.credentials}`,
				'X-App-User': store.settings.username ?? ""
			},
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
			const error = await response.json() as ServerError[];
			if (store.server)
				store.server.serverErrors = store.server.serverErrors.concat(error);
		}
	}
	catch (error)
	{
		console.error('Netzwerkfehler beim Upload:', error)
	}
	return false;
}

export async function uploadFiles(event: Event, fileOrFolder: string): Promise<boolean>
{
	const target = event.target as HTMLInputElement;
	if (!target.files || target.files.length === 0) return false;

	const formData = new FormData();
	formData.append('fileOrFolder', fileOrFolder);

	Array.from(target.files).forEach((file) => {
		formData.append('files', file);
	});

	return await sendUploadRequest(formData);
}

export async function uploadEditorContent(path: string, content: string, final: boolean = false): Promise<boolean>
{
	const formData = new FormData();
	
	const ext = getFileExtension(path);
	if (!ext) return false;
	
	const file = new File([content], getFilename(path), { type: 'text/plain' });

	formData.append('fileOrFolder', path);
	formData.append('files', file);
	if (final)
		formData.append('unlock', 'true');

	return await sendUploadRequest(formData);
}

export async function updatePreview(el: RepositoryElement): Promise<FileObject | null>
{
	const response = await fetchWithView(`/api/preview?file=${el.path}`);
	if (response.ok)
	{
		return await response.json() as FileObject;
	}
	else
	{
		return null;
	}
}

export async function sendPluginData(data: PluginRequest)
{
	const options: RequestInit = {
		method: "POST",
		body: JSON.stringify(data)
	};
	const resp = await fetch("/api/plugin", options);
	return resp.ok;
}
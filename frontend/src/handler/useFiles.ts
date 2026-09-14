import { fetchWithView } from "@/client";
import emitter from "@/mitt";
import { useStore } from "@/store";
import type { FileLockRequest, FileLockResponse, RepositoryElement, ServerError } from "@/types/vivien-generated";
import { ref, type Ref } from "vue";

export function useFiles()
{
	const isLoading = ref(false);

	const createFolder = (parent: string, name: string) => doCreateFolder(isLoading, parent, name);
	const lockFile = (file: string, lock: boolean) => doLockFile(isLoading, file, lock);

	return {
		createFolder,
		lockFile,
		isLoading,
	};
}

async function doCreateFolder(isLoading: Ref<boolean>, parent: string, name: string): Promise<boolean>
{
	try
	{
		isLoading.value = true;

		const response = await fetchWithView('/api/create', {
			method: 'POST',
			body: JSON.stringify({
				name: name,
				path: parent,
				type: 'FOLDER'
			} as RepositoryElement)
		})
		
		if (response.ok)
		{
			emitter.emit("refresh-folder");
			return true;
		}
		else
		{
			const error = await response.json() as ServerError;
			emitter.emit("error", error);
		}
	}
	catch(err: unknown)
	{
		emitter.emit("error", err as Error);
	}
	finally
	{
		isLoading.value = false;
	}
	return false;
}

async function doLockFile(isLoading: Ref<boolean>, file: string, lock: boolean): Promise<FileLockResponse>
{
	try
	{
		const store = useStore();
		isLoading.value = true;

		const response = await fetchWithView('/api/filelock', {
			method: 'POST',
			body: JSON.stringify({
				user: store.settings.username,
				file,
				lock
			} as FileLockRequest)
		})
		
		if (response.ok)
		{
			return await response.json() as FileLockResponse;
		}
		else
		{
			const error = await response.json() as ServerError;
			emitter.emit("error", error);
		}
	}
	catch(err: unknown)
	{
		emitter.emit("error", err as Error);
	}
	finally
	{
		isLoading.value = false;
	}
	return { success: false };
}
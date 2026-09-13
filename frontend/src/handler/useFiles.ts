import { fetchWithView } from "@/client";
import emitter from "@/mitt";
import type { MoveRequest, RepositoryElement, ServerError } from "@/types/vivien-generated";
import { ref, type Ref } from "vue";

export function useFiles()
{
	const isLoading = ref(false);

	const createFolder = (parent: string, name: string) => doCreateFolder(isLoading, parent, name);

	return {
		createFolder,
		isLoading,
	};
}

async function doCreateFolder(isLoading: Ref<boolean>, parent: string, name: string,): Promise<boolean>
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
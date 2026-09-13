import emitter from "@/mitt";
import { sendCheckout, sendCommit, sendDelete, sendMove, sendPull, sendReset } from "@/services/git";
import { useStore, type StoreType } from "@/store";
import type { GitBranchStatus, ServerError } from "@/types/vivien-generated";
import { ref, type Ref } from "vue";

export function useGit()
{
	const store = useStore();
    const isLoading = ref(false);

	const checkout = (branch: string) => doCheckout(store, isLoading, branch);
	const commit = (message: string) => doCommit(store, isLoading, message);
	const reset = () => doReset(store, isLoading);
	const pull = () => doPull(store, isLoading);
	const $delete = (file: string) => doDelete(store, isLoading, file);
	const move = (src: string, dst: string) => doMoveElement(store, isLoading, src, dst);

	return {
		checkout,
		commit,
		reset,
		pull,
		$delete,
		move,
		isLoading,
	};
}

async function doCheckout(store: StoreType, isLoading: Ref<boolean>, branch: string)
{
	try
	{
		isLoading.value = true;

		const response = await sendCheckout(branch);

		if (response.ok)
		{
			store.git = await response.json() as GitBranchStatus
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
}

async function doPull(store: StoreType, isLoading: Ref<boolean>)
{
	try
	{
		isLoading.value = true;

		const response = await sendPull();

		if (response.ok)
		{
			store.git = await response.json() as GitBranchStatus
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
}

async function doReset(store: StoreType, isLoading: Ref<boolean>)
{
	try
	{
		isLoading.value = true;

		const response = await sendReset();

		if (response.ok)
		{
			store.git = await response.json() as GitBranchStatus
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
}

async function doCommit(store: StoreType, isLoading: Ref<boolean>, message: string)
{
	try
	{
		if (!store.settings.username) throw new Error("username ist null");
		if (!store.settings.email) throw new Error("email ist null");
		isLoading.value = true;

		const response = await sendCommit(
			store.settings.username,
			store.settings.email,
			message);

		if (response.ok)
		{
			store.git = await response.json() as GitBranchStatus
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
}

async function doDelete(store: StoreType, isLoading: Ref<boolean>, file: string)
{
	try
	{
		isLoading.value = true;

		const response = await sendDelete(file);

		if (response.ok)
		{
			store.git = await response.json() as GitBranchStatus;
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
}

async function doMoveElement(store: StoreType, isLoading: Ref<boolean>, src: string, dst: string): Promise<boolean>
{
	try
	{
		isLoading.value = true;

		const response = await sendMove(src, dst);
		
		if (response.ok)
		{
			store.git = await response.json() as GitBranchStatus;
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
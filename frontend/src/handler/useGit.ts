import emitter from "@/mitt";
import { sendCheckout, sendCommit, sendDelete, sendFetch, sendReset } from "@/services/git";
import { useStore, type StoreType } from "@/store";
import type { GitBranchStatus } from "@/types/vivien-generated";
import { ref, type Ref } from "vue";

export function useGit()
{
	const store = useStore();
    const isLoading = ref(false);

	const checkout = (branch: string) => doCheckout(store, isLoading, branch);
	const fetch = () => doFetch(store, isLoading);
	const commit = (message: string) => doCommit(store, isLoading, message);
	const reset = () => doReset(store, isLoading);
	const $delete = (file: string) => doDelete(store, isLoading, file);

	return {
		checkout,
		fetch,
		commit,
		reset,
		$delete,
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
			emitter.emit("error", new Error(`Checkout fehlgeschlagen: ${response.status}`));
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

async function doFetch(store: StoreType, isLoading: Ref<boolean>)
{
	try
	{
		isLoading.value = true;

		const response = await sendFetch();

		if (response.ok)
		{
			store.git = await response.json() as GitBranchStatus
		}
		else
		{
			emitter.emit("error", new Error(`Fetch fehlgeschlagen: ${response.status}`));
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
			emitter.emit("error", new Error(`Reset fehlgeschlagen: ${response.status}`));
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
			emitter.emit("error", new Error(`Commit fehlgeschlagen: ${response.status}`));
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
			store.git = await response.json() as GitBranchStatus
		}
		else
		{
			emitter.emit("error", new Error(`Löschen fehlgeschlagen: ${response.status}`));
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
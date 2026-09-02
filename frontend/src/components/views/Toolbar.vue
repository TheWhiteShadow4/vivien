<!-- eslint-disable vue/multi-word-component-names -->
<!-- src/components/views/Toolbar.vue -->
<script setup lang="ts">
import IconPlus from '@/icons/IconPlus.vue';
import BaseIconButton from '../base/BaseIconButton.vue';
import IconMinus from '@/icons/IconMinus.vue';
import IconUpload from '@/icons/IconUpload.vue';
import IconDownload from '@/icons/IconDownload.vue';
import { useStore } from '@/store/index.ts';
import type { GitBranchStatus, GitStageOperation, RepositoryElement, ServerError } from '@/types/vivien-generated.js';
import { computed, ref } from 'vue';
import { sendChangeStaged, uploadFiles } from '@/client.ts';
import emitter from '@/mitt.ts';
import IconClose from '@/icons/IconClose.vue';
import IconSync from '@/icons/IconSync.vue';
import { sendDelete } from '@/services/git.ts';

const store = useStore();

const props = defineProps<{
  element: RepositoryElement
}>();

const isLoading = ref<boolean>(false)

const canTrack = computed(() => {
	return store.git && store.git.untracked.indexOf(props.element.path) != -1;
});

const canUntrack = computed(() => {
	return store.git && store.git.added.indexOf(props.element.path) != -1;
});

const isRemoved = computed(() => {
	return store.git && store.git.removed.indexOf(props.element.path) != -1;
});

const canDelete = computed(() => {
	return store.git && store.git.missing.indexOf(props.element.path) == -1
					 && !isRemoved.value ;
});

async function changeStaged(op: GitStageOperation)
{
	try
	{
		if (isLoading.value) return;
		const response = await sendChangeStaged(props.element.path, op);
		if (response.ok)
		{
			store.git = await response.json() as GitBranchStatus
		}
	}
	catch (err: unknown)
	{
		emitter.emit("error", { message: (err as Error).message} as ServerError);
	}
	finally
	{
		isLoading.value = false
	}
}

async function deleteFile()
{
	try
	{
		if (isLoading.value) return;

		if (canTrack.value)
		{
			// Datei ist nicht im Git, wir müssen sie normal löschen.
			const response = await sendDelete(props.element.path);
			if (response.ok)
			{
				emitter.emit("refresh-folder");
				emitter.emit("refresh-preview");
			}
		}
		else
		{
			const response = await sendChangeStaged(props.element.path, "Delete");
			if (response.ok)
			{
				store.git = await response.json() as GitBranchStatus
			}
		}
	}
	catch (err: unknown)
	{
		emitter.emit("error", { message: (err as Error).message} as ServerError);
	}
	finally
	{
		isLoading.value = false
	}
}


async function download()
{
	try
	{
		if (isLoading.value) return;

		const response = await fetch(`/api/download?file=${props.element.path}`);

		if (response.ok)
		{
			const blob = await response.blob();
			const link = document.createElement('a')
			link.href = URL.createObjectURL(blob)
			link.download = props.element.name;
			link.click();
			URL.revokeObjectURL(link.href);
		}
		else
		{
			emitter.emit("error", {message: "Download fehlgeschlagen"} as ServerError);
		}
	}
	finally
	{
		isLoading.value = false
	}
}

const fileInput = ref<HTMLInputElement | null>(null);

function openFileBrowser()
{
	fileInput.value?.click();
}

async function handleFileChange(event: Event)
{
	try
	{
		if (isLoading.value) return;

		const success = await uploadFiles(event, props.element.path);
		if (success)
		{
			emitter.emit("refresh-preview", props.element);
		}
	}
	finally
	{
		isLoading.value = false
	}
}

</script>

<template>
	<nav class="bg-vit-surface w-full h-24 border border-vit-border flex gap-2 p-2">
		<BaseIconButton
			v-if="canTrack"
			@click="changeStaged('Track')"
			:disabled="isLoading"
			variant="normal" size="xl"
			class="flex flex-col items-center">
			<IconPlus />
			<span class="text-sm">Add</span>
		</BaseIconButton>

		<BaseIconButton
			v-if="canUntrack"
			@click="changeStaged('Untrack')"
			:disabled="isLoading"
			variant="normal" size="xl" 
			class="flex flex-col items-center">
			<IconMinus />
			<span class="text-sm">Remove</span>
		</BaseIconButton>
		
		<BaseIconButton
			@click="openFileBrowser()"
			:disabled="isLoading || isRemoved"
			variant="normal" size="xl" 
			class="flex flex-col items-center">
			<IconUpload />
			<span class="text-sm">Ersetzen</span>
		</BaseIconButton>

		<BaseIconButton
			@click="download()"
			:disabled="isLoading || isRemoved"
			variant="normal" size="xl"
			class="flex flex-col items-center">
			<IconDownload />
			<span class="text-sm">Download</span>
		</BaseIconButton>

		<BaseIconButton
			v-if="isRemoved"
			@click="changeStaged('Undelete')"
			:disabled="isLoading"
			variant="normal" size="xl"
			class="ml-6 flex flex-col items-center">
			<IconSync />
			<span class="text-sm">Zurück</span>
		</BaseIconButton>

		<BaseIconButton
			v-if="canDelete"
			@click="deleteFile()"
			:disabled="isLoading"
			variant="danger" size="xl"
			class="ml-6 flex flex-col items-center">
			<IconClose />
			<span class="text-sm">Löschen</span>
		</BaseIconButton>

		<input 
			type="file" 
			ref="fileInput" 
			style="display: none"
			multiple
			@change="handleFileChange" 
		/>
	</nav>
</template>
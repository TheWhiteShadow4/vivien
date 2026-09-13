<!-- src\components\dialoge\NameDialog.vue -->
<script setup lang="ts">
import BaseButton from '@/base/BaseButton.vue';
import BasePanel from '@/base/BasePanel.vue';
import TextInput from '@/base/TextInput.vue';
import { useFiles } from '@/handler/useFiles';
import { ref } from 'vue';

const props = defineProps<{ parent: string }>();

const value = ref('');
const isLoading = ref<boolean>(false);

const emit = defineEmits(["close"])

const { createFolder } = useFiles();

async function submit()
{
	await createFolder(props.parent, value.value);
	emit("close"); // App.vue
}

function cancel()
{
	emit("close"); // App.vue
}

const backdropStyles = 'fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm'
const titleStyles = `text-xl font-bold text-vit-text-main font-sans
tracking-wide border-b border-vit-border pb-2 mb-4`
</script>

<template>
	<div :class="backdropStyles" @click.self.prevent>
	<BasePanel variant="dialog">
		
		<h2 :class="titleStyles">Neuen Ordner erstellen</h2>

		<div class="flex flex-col gap-6 w-108">
		<TextInput
			v-model="value"
			type="text"
			label="Name"
			placeholder="Name"
			variant="default"
			@enter="submit"
		>
		</TextInput>
		</div>

		<div class="mt-6 flex justify-between">
		<BaseButton
			variant="primary"
			:disabled="isLoading"
			@click="submit"
		>Bestätigen</BaseButton>
			<BaseButton
			variant="danger"
			:disabled="isLoading"
			@click="cancel"
		>Abbrechen</BaseButton>
		</div>

	</BasePanel>
	</div>
</template>
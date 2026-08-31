<!-- src\components\dialoge\CommitDialog.vue -->
<script setup lang="ts">
import { useStore } from '@/store';
import TextInput from '../base/TextInput.vue'
import { ref, computed } from 'vue'
import BasePanel from '../base/BasePanel.vue';
import BaseButton from '../base/BaseButton.vue';
import { sendCommit } from '@/client.ts';
import emitter from '@/mitt.ts';


const store = useStore();

const emit = defineEmits(["submit", "cancel"])

const message = ref('');
const isLoading = ref(false);

// Validierung: Name darf nicht leer sein, E-Mail braucht eine Grundstruktur
const isMessageValid = computed(() => message.value.trim().length > 0)
const isFormValid = computed(() => store.settings.email && isMessageValid.value)

const backdropStyles = 'fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm'
const titleStyles = `text-xl font-bold text-vit-text-main font-sans
tracking-wide border-b border-vit-border pb-2 mb-4`

async function submitCommit()
{
	if (!isFormValid.value) return
	try
	{
		isLoading.value = true;

		const response = await sendCommit(message.value);

		if (!response.ok)
		{
			emitter.emit("error", new Error(`Commit fehlgeschlagen: ${response.status}`));
		}
		emit("submit")
	}
	finally
	{
		isLoading.value = false;
	}
}

function cancelCommit()
{
	emit("cancel");
}
</script>

<template>
  <div :class="backdropStyles" @click.self.prevent>
    <BasePanel variant="dialog">
      
      <h2 :class="titleStyles">Commit</h2>

      <div class="flex flex-col gap-6 w-108">
        <TextInput
          v-model="message"
          type="text"
          label="Nachicht"
          placeholder="Message"
          :variant="message && !isMessageValid ? 'failed' : 'default'"
          @enter="submitCommit"
        >
        </TextInput>
      </div>

      <div class="mt-6 flex justify-between">
        <BaseButton
          variant="primary"
          :disabled="!isFormValid || isLoading"
          @click="submitCommit"
        >Absenden</BaseButton>
		 <BaseButton
          variant="danger"
          :disabled="isLoading"
          @click="cancelCommit"
        >Abbrechen</BaseButton>
      </div>

    </BasePanel>
  </div>
</template>
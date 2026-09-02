<!-- src\components\dialoge\CommitDialog.vue -->
<script setup lang="ts">
import { useStore } from '@/store';
import { ref, computed, onMounted } from 'vue'
import BasePanel from '../base/BasePanel.vue';
import BaseButton from '../base/BaseButton.vue';
import { useGit } from '@/handler/useGit.ts';
import TextArea from '../base/TextArea.vue';

const store = useStore();
const message = ref('');

const emit = defineEmits(["submit", "cancel"])

const { commit, isLoading } = useGit();

const isMessageValid = computed(() => message.value.trim().length > 0)
const isFormValid = computed(() => store.settings.email && isMessageValid.value)

async function submitCommit()
{
	if (!isFormValid.value) return
	await commit(message.value);
	emit("submit");
}

function cancelCommit()
{
	emit("cancel");
}

onMounted(() => 
{
	if (!store.git) return;

	const builder = [];
	if (store.git.added.length > 0)
		builder.push(`${store.git.added.length} Assets hinzugefügt.`);
	if (store.git.changed.length > store.git.added.length)
		builder.push(`${store.git.changed.length - store.git.added.length} Assets geändert.`);
	if (store.git.removed.length > 0)
		builder.push(`${store.git.removed.length} Assets gelöscht.`);
	message.value = builder.join("\n");
})

const backdropStyles = 'fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm'
const titleStyles = `text-xl font-bold text-vit-text-main font-sans
tracking-wide border-b border-vit-border pb-2 mb-4`
</script>

<template>
  <div :class="backdropStyles" @click.self.prevent>
    <BasePanel variant="dialog">
      
      <h2 :class="titleStyles">Commit</h2>

      <div class="flex flex-col gap-6 w-108">
        <TextArea
          v-model="message"
          type="text"
          label="Nachicht (erforderlich)"
          placeholder="Message"
          :variant="message && !isMessageValid ? 'failed' : 'default'"
          @enter="submitCommit"
        >
        </TextArea>
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
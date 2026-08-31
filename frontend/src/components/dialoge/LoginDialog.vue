<!-- src\components\dialoge\LoginDialog.vue -->
<script setup lang="ts">
import { useStore } from '@/store';
import TextInput from '../base/TextInput.vue'
import { ref, computed } from 'vue'
import BasePanel from '../base/BasePanel.vue';
import BaseButton from '../base/BaseButton.vue';


const store = useStore();

const emit = defineEmits()

const name = ref('')
const email = ref('')

// Validierung: Name darf nicht leer sein, E-Mail braucht eine Grundstruktur
const isNameValid = computed(() => name.value.trim().length > 0)
const isEmailValid = computed(() => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email.value.trim())
})
const isFormValid = computed(() => isNameValid.value && isEmailValid.value)

const backdropStyles = 'fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm'
const titleStyles = `text-xl font-bold text-vit-text-main font-sans
tracking-wide border-b border-vit-border pb-2 mb-4`

function submitLogin() {
  if (!isFormValid.value) return

  store.updateSetting("username", name.value.trim());
  store.updateSetting("email", email.value.trim());
  
  emit('complete')
}
</script>

<template>
  <div :class="backdropStyles" @click.self>
    <BasePanel variant="dialog">
      
      <h2 :class="titleStyles">Login</h2>

      <div class="flex flex-col gap-6 w-108">
        <TextInput
          v-model="name"
          type="text"
          label="Name"
          placeholder="Name"
          :variant="name && !isNameValid ? 'failed' : 'default'"
          @enter="submitLogin"
        >
        </TextInput>

        <TextInput
          v-model="email"
          type="text"
          label="E-Mail-Adresse"
          placeholder="Email"
          :variant="email && !isEmailValid ? 'failed' : 'default'"
          @enter="submitLogin"
        >
        </TextInput>
      </div>

      <div class="mt-6">
        <BaseButton
          variant="primary"
          :disabled="!isFormValid"
          @click="submitLogin"
        >Login</BaseButton>
      </div>

    </BasePanel>
  </div>
</template>
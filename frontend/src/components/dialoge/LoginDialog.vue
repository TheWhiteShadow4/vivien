<!-- src\components\dialoge\LoginDialog.vue -->
<script setup lang="ts">
import { useStore } from '@/store';
import TextInput from '../base/TextInput.vue'
import { ref, computed } from 'vue'
import BasePanel from '../base/BasePanel.vue';
import BaseButton from '../base/BaseButton.vue';


const store = useStore();

const emit = defineEmits(["submit", "cancel"])

const name = ref(store.settings.username ?? '')
const email = ref(store.settings.email ?? '')
const password = ref('')

// Validierung: Name darf nicht leer sein, E-Mail braucht eine Grundstruktur
const isNameValid = computed(() => name.value.trim().length > 0)
const isEmailValid = computed(() => {
	const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
	return emailRegex.test(email.value.trim())
})
const isFormValid = computed(() => isNameValid.value && isEmailValid.value)
const canCancelt = computed(() => store.settings.username != null && store.settings.email != null)

const backdropStyles = 'fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm'
const titleStyles = `text-xl font-bold text-vit-text-main font-sans
tracking-wide border-b border-vit-border pb-2 mb-4`

function submitLogin()
{
	if (!isFormValid.value) return

	const tname = name.value.trim();

	store.updateSetting("username", tname);
	store.updateSetting("email", email.value.trim());

	if (password.value.length > 0)
	{
		const credentials = btoa(`${tname}:${password.value}`)
		store.updateSetting("credentials", credentials);
	}
	
	emit("submit"); // App.vue
}
</script>

<template>
	<div :class="backdropStyles" @click.self.prevent>
		<BasePanel variant="dialog">

			<h2 :class="titleStyles">Login</h2>

			<div class="flex flex-col gap-6 w-108">
				<TextInput v-model="name" type="text" label="Anzeige Name" placeholder=""
					:variant="name && !isNameValid ? 'failed' : 'default'">
				</TextInput>

				<TextInput v-model="email" type="text" label="E-Mail-Adresse" placeholder=""
					:variant="email && !isEmailValid ? 'failed' : 'default'">
				</TextInput>

				<TextInput v-model="password" type="password" label="Server Passwort" placeholder=""
					:variant="password ? 'failed' : 'default'">
				</TextInput>
			</div>

			<div class="mt-6 flex justify-between">
				<BaseButton variant="primary" :disabled="!isFormValid" @click="submitLogin">Login</BaseButton>
				<BaseButton v-if="canCancelt" variant="danger" @click="emit('cancel')">Abbrechen</BaseButton>
			</div>

		</BasePanel>
	</div>
</template>
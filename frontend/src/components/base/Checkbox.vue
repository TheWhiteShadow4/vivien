<!-- eslint-disable vue/multi-word-component-names -->
<!-- src/components/base/Checkbox.vue -->
<script setup lang="ts">
import BaseIconButton from '@/base/BaseIconButton.vue';
import IconClose from '@/icons/IconClose.vue';
import IconGitCommit from '@/icons/IconGitCommit.vue';

const model = defineModel<boolean>({ required: true });
</script>

<template>
	<BaseIconButton size="sm" @click="model = !model">
		<IconGitCommit v-if="model == true" class="icon"/>
		<IconClose v-if="model == false" class="icon"/>
	</BaseIconButton>
</template>
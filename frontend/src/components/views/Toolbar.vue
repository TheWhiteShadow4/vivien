<!-- eslint-disable vue/multi-word-component-names -->
<!-- src/components/views/Toolbar.vue -->
<script setup lang="ts">
import IconPlus from '@/icons/IconPlus.vue';
import BaseIconButton from '../base/BaseIconButton.vue';
import IconMinus from '@/icons/IconMinus.vue';
import IconUpload from '@/icons/IconUpload.vue';
import IconDownload from '@/icons/IconDownload.vue';

</script>

<template>
	<div>
	<BaseIconButton>
		<IconPlus />
	</BaseIconButton>
	<BaseIconButton>
		<IconMinus />
	</BaseIconButton>
	<BaseIconButton>
		<IconUpload />
	</BaseIconButton>
		<BaseIconButton>
		<IconDownload />
	</BaseIconButton>
	</div>
</template>
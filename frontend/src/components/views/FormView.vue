<!-- src\views\FormView.vue -->
<script setup lang="ts">
import { ref } from "vue";
import schema from "./schema.json"
import TextInput from "@/base/TextInput.vue";
import Checkbox from "@/base/Checkbox.vue";
import NumberInput from "@/base/NumberInput.vue";
import SelectInput from "@/base/SelectInput.vue";
import BaseButton from "@/base/BaseButton.vue";
import { sendPluginData } from "@/client";
import type { PluginRequest } from "@/types/vivien-generated";

interface Props {
	label?: string
	id?: string
	disabled?: boolean
}
defineProps<Props>();

const data = ref<Record<string, string | number | boolean | null>>({
	text: "Anna",
	janein: false,
	nummer: 0,
	list: null,
	referenz: null
});

function getstr(key: string): string
{
	if (typeof(data.value[key]) == "string")
		return data.value[key];
	else
		return "";
}

function getbool(key: string): boolean
{
	return (typeof(data.value[key]) == "boolean") ? data.value[key] : false;
}

function getint(key: string): number
{
	return (typeof(data.value[key]) == "number") ? data.value[key] : 0;
}

function id(key: string): string
{
	return `form-${key}`;
}

function onSubmit()
{
	console.log("Senden")
	sendPluginData({ file: , data: data.value } as PluginRequest);
}
</script>

<template>
	<div class="@container">
	<div class="grid @xs:grid-cols-1 @lg:grid-cols-2 gap-2 gap-x-8">
		<div v-for="(value, key) in schema" :key="key" class="grid grid-cols-[40%_60%] p-2">
			<span class="text-md text-vit-text-muted"
				v-if="value.label !== false" :for="id(key)">
				{{ value.label ?? key }}
			</span>
			<TextInput
				v-if="value.type == 'string'"
				:id="id(key)"
				:small="false"
				:model-value="getstr(key)"
			/>
			<Checkbox
				v-if="value.type == 'bool'"
				:model-value="getbool(key)"
			/>
			<NumberInput
				v-if="value.type == 'int'"
				:model-value="getint(key)"
			/>
			<SelectInput
				v-if="value.type == 'enum' && value.values"
				:model-value="getint(key)"
				:values="value.values"
			/>
			<TextInput
				v-if="value.type == 'asset'"
				type="search"
				:small="false"
				:model-value="getstr(key)"
			/>
		</div>
	</div>
	<BaseButton @click="onSubmit">Senden</BaseButton>
	</div>
</template>
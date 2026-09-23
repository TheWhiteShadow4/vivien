<!-- src\views\FormView.vue -->
<script setup lang="ts">
import { reactive } from "vue";
import TextInput from "@/base/TextInput.vue";
import Checkbox from "@/base/Checkbox.vue";
import NumberInput from "@/base/NumberInput.vue";
import SelectInput from "@/base/SelectInput.vue";
import BaseButton from "@/base/BaseButton.vue";
import { sendPluginData } from "@/client";
import type { FileObject, PluginRequest, TypedData } from "@/types/vivien-generated";


const props = defineProps<{
	fileObject: FileObject
}>();

const data = reactive<TypedData[]>(props.fileObject.fileParams);

function getstr(data: TypedData): string
{
	return data.value as string;
}

function getbool(data: TypedData): boolean
{
	return (data.type == "bool") ? data.value as boolean : false;
}

function getint(data: TypedData): number
{
	return (data.type == "int") ? data.value as number : 0;
}

function id(key: string): string
{
	return `form-${key}`;
}

function onSubmit()
{
	console.log("Senden")
	sendPluginData({ file: props.fileObject.filename, data: data } as PluginRequest);
}
</script>

<template>
	<div class="@container">
	<h2 class="text-lg p-1 mb-2">Import Einstellungen</h2>
	<div class="grid @xs:grid-cols-1 @lg:grid-cols-2 gap-2 gap-x-8">
		<div v-for="(data, index) in fileObject.fileParams" :key="data.name" class="grid grid-cols-[40%_60%] p-1">
			<span class="text-md text-vit-text-muted"
				:for="id(data.name)">
				{{ data.label }}
			</span>
			<TextInput
				v-if="data.type == 'string'"
				:id="id(data.name)"
				:small="false"
				v-model="data.value"
			/>
			<Checkbox
				v-if="data.type == 'bool'"
				v-model="data.value"
			/>
			<NumberInput
				v-if="data.type == 'int' && !data.options"
				v-model="data.value"
			/>
			<SelectInput
				v-if="data.type == 'int' && data.options"
				mode="index"
				v-model="data.value"
				:values="data.options"
			/>
			<SelectInput
				v-if="data.type == 'enum' && data.options"
				mode="value"
				v-model="data.value"
				:values="data.options"
			/>
			<TextInput
				v-if="data.type == 'asset'"
				type="search"
				:small="false"
				v-model="data.value"
			/>
			</div>
		</div>
		<div class="flex justify-end m-2">
			<BaseButton @click="onSubmit">Übernehmen</BaseButton>
		</div>
	</div>
</template>
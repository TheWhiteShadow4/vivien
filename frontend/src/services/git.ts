import { fetchWithView } from "@/client";
import type { CheckoutRequest, CommitRequest, GitStageRequest } from "@/types/vivien-generated";


export async function sendCheckout(branch: string): Promise<Response>
{
	const options: RequestInit = {
		method: "POST",
		body: JSON.stringify({
			branch,
		} as CheckoutRequest)
	};
	return fetchWithView("/api/checkout", options);
}

export async function sendFetch(): Promise<Response>
{
	const options: RequestInit = {
		method: "POST"
	};
	return fetchWithView("/api/fetch", options);
}

export async function sendPull(): Promise<Response>
{
	const options: RequestInit = {
		method: "POST"
	};
	return fetchWithView("/api/pull", options);
}

export async function sendCommit(name: string, email: string, message: string): Promise<Response>
{
	const options: RequestInit = {
		method: "POST",
		body: JSON.stringify({
			name,
			email,
			message
		} as CommitRequest)
	};
	return fetchWithView("/api/commit", options);
}

export async function sendReset(): Promise<Response>
{
	const options: RequestInit = {
		method: "POST"
	};
	return fetchWithView("/api/reset", options);
}

export async function sendDelete(file: string): Promise<Response>
{
	const options: RequestInit = {
		method: "POST",
		body: JSON.stringify({
			op: "Delete",
			email: "",
			file
		} as GitStageRequest)
	};
	return fetchWithView("/api/delete", options);
}
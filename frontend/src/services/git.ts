import type { CheckoutRequest, CommitRequest, GitStageRequest } from "@/types/vivien-generated";


export async function sendCheckout(branch: string): Promise<Response>
{
	const options: RequestInit = {
		method: "POST",
		headers: {'Content-Type': 'application/json'},
		body: JSON.stringify({
			branch,
		} as CheckoutRequest)
	};
	return fetch("/api/checkout", options);
}

export async function sendFetch(): Promise<Response>
{
	const options: RequestInit = {
		method: "POST",
		headers: {'Content-Type': 'application/json'}
	};
	return fetch("/api/fetch", options);
}

export async function sendCommit(name: string, email: string, message: string): Promise<Response>
{
	const options: RequestInit = {
		method: "POST",
		headers: {'Content-Type': 'application/json'},
		body: JSON.stringify({
			name,
			email,
			message
		} as CommitRequest)
	};
	return fetch("/api/commit", options);
}

export async function sendReset(): Promise<Response>
{
	const options: RequestInit = {
		method: "POST",
		headers: {'Content-Type': 'application/json'}
	};
	return fetch("/api/reset", options);
}

export async function sendDelete(file: string): Promise<Response>
{
	const options: RequestInit = {
		method: "POST",
		headers: {'Content-Type': 'application/json'},
		body: JSON.stringify({
			op: "Delete",
			email: "",
			file
		} as GitStageRequest)
	};
	return fetch("/api/delete", options);
}
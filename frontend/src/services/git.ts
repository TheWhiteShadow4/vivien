import { fetchWrapper } from '@/client'
import type { CheckoutRequest, CommitRequest, GitStageRequest, MoveRequest } from "@/types/vivien-generated";


export async function sendCheckout(branch: string): Promise<Response>
{
	const options: RequestInit = {
		method: "POST",
		body: JSON.stringify({
			branch,
		} as CheckoutRequest)
	};
	return fetchWrapper('/api/checkout', options)
}

export async function sendPull(): Promise<Response>
{
	const options: RequestInit = {
		method: "POST"
	};
	return fetchWrapper('/api/pull', options)
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
	return fetchWrapper('/api/commit', options)
}

export async function sendReset(): Promise<Response>
{
	const options: RequestInit = {
		method: "POST"
	};
	return fetchWrapper('/api/reset', options)
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
	return fetchWrapper('/api/delete', options)
}

export async function sendMove(src: string, dst: string): Promise<Response>
{
	const options: RequestInit = {
		method: "POST",
		body: JSON.stringify({ src, dst } as MoveRequest)
	};
	return fetchWrapper('/api/move', options)
}

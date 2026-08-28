/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 4.1.1 on 2026-08-27 10:44:38.

export interface LoginRequest {
    username: string;
    password: string;
}

export interface RepositoryElement {
    name: string;
    type: ElementType;
    children: RepositoryElement[];
}

export interface RepositoryView {
    elements: RepositoryElement[];
}

export interface ServerError {
    message: string;
    stacktrace: string;
}

export interface ServerState {
    view: string;
    mode: ServerMode;
    user?: User;
    serverErrors: ServerError[];
}

export interface User {
    name: string;
}

export type ElementType = "ROOT" | "FOLDER" | "FILE";

export type ServerMode = "LOCAL" | "HOSTED" | "SETUP" | "SAFE";

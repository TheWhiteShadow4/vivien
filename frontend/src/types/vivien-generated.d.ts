/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 4.1.1 on 2026-08-26 16:44:43.

export interface LoginRequest {
    username: string;
    password: string;
}

export interface Repository {
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

export interface RepositoryElement {
    name: string;
    type: ElementType;
    childs: RepositoryElement[];
}

export type ServerMode = "LOCAL" | "HOSTED" | "SETUP" | "SAFE";

export type ElementType = "FOLDER" | "FILE";

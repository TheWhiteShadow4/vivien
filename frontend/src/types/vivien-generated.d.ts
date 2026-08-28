/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 4.1.1 on 2026-08-28 17:39:19.

export interface FileObject {
    url: string;
    hash: string;
    mimeType: string;
    size: number;
    width: number;
    height: number;
}

export interface RepositoryElement {
    name: string;
    path: string;
    type: ElementType;
    children: RepositoryElement[];
    gitStatus: GitStatus;
    lazy: boolean;
    parent?: RepositoryElement;
}

export interface RepositoryView extends RepositoryElement {
}

export interface ServerError {
    message: string;
    stacktrace: string;
}

export interface ServerState {
    view: string;
    mode: ServerMode;
    user?: ServerUser;
    serverErrors: ServerError[];
}

export interface ServerUser {
    name: string;
}

export interface UserSettings {
    username?: string;
    view: string;
}

export type ElementType = "FOLDER" | "FILE";

export type GitStatus = "Same" | "Added" | "Modified" | "Deleted" | "Moved";

export type ServerMode = "LOCAL" | "HOSTED" | "SETUP" | "SAFE";

/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 4.1.1 on 2026-08-29 11:14:40.

export interface FileObject {
    url: string;
    filename: string;
    metadata: FileObjectMeta;
}

export interface RepositoryElement {
    name: string;
    path: string;
    type: ElementType;
    children?: RepositoryElement[];
    gitStatus: GitStatus;
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

export interface FileObjectMeta {
    mimeType: string;
    size: number;
    width: number;
    height: number;
    srcWidth: number;
    srcHeight: number;
    importProps?: { [index: string]: any };
}

export type ElementType = "FOLDER" | "FILE";

export type GitStatus = "Untracked" | "Same" | "Added" | "Modified" | "Deleted" | "Conflict";

export type ServerMode = "LOCAL" | "HOSTED" | "SETUP" | "SAFE";

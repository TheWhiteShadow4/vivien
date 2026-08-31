/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 4.1.1 on 2026-08-30 14:48:32.

export interface FileObject {
    url: string;
    filename: string;
    metadata: FileObjectMeta;
}

export interface GitBranchStatus {
    branch: string;
    remote: RemoteGitStatus;
    modified: boolean;
    untracked: string[];
    added: string[];
    changed: string[];
    missing: string[];
    conflicts: string[];
}

export interface RemoteGitStatus {
    behindCount: number;
    aheadCount: number;
}

export interface RepositoryElement {
    name: string;
    path: string;
    type: ElementType;
    children?: RepositoryElement[];
    gitStatus?: GitFileStatus;
    parent?: RepositoryElement;
}

export interface RepositoryRoot extends RepositoryElement {
}

export interface ServerError {
    message: string;
    stacktrace: string;
}

export interface ServerState {
    view: string;
    mode: ServerMode;
    branch?: string;
    user?: ServerUser;
    serverErrors: ServerError[];
}

export interface ServerUser {
    name: string;
}

export interface UserSettings {
    username?: string;
    email?: string;
    view: string;
    sidebar: boolean;
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

export type ElementType = "ROOT" | "FOLDER" | "FILE" | "VIRTUAL";

export type GitFileStatus = "Untracked" | "Clean" | "Added" | "Modified" | "Deleted" | "Conflict";

export type ServerMode = "LOCAL" | "HOSTED" | "SETUP" | "SAFE";

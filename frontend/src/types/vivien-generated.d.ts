/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 4.1.1 on 2026-09-02 15:06:59.

export interface CheckoutRequest {
    branch: string;
}

export interface CommitRequest {
    name: string;
    email: string;
    message: string;
}

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
    removed: string[];
    missing: string[];
    conflicts: string[];
}

export interface GitStageRequest {
    op: GitStageOperation;
    email: string;
    file: string;
}

export interface ImportSetting {
    name: string;
    displayLabel: string;
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

export interface ServerResult {
    success: boolean;
    message: string;
    error: ServerError;
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

export interface StageInfo {
    added: number;
    removed: number;
    modified: number;
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

export interface Success {
    success: true;
    message: string;
}

export interface Failure {
    success: false;
    error: ServerError;
}

export type ElementType = "ROOT" | "FOLDER" | "FILE" | "VIRTUAL";

export type GitFileStatus = "Untracked" | "Clean" | "Added" | "Modified" | "Deleted" | "Conflict";

export type GitStageOperation = "Track" | "Untrack" | "Delete" | "Undelete";

export type ServerMode = "LOCAL" | "HOSTED" | "SETUP" | "SAFE";

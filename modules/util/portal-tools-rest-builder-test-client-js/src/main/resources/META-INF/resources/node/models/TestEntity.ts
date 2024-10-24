/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { NestedTestEntity } from './NestedTestEntity';
/**
 * https://www.schema.org/Document
 */
export type TestEntity = {
    customFields?: Record<string, any>;
    expirationDate?: string;
    dateCreated?: string;
    dateModified?: string;
    priority?: number;
    folderId?: number;
    groupId?: number;
    creatorId?: number;
    viewCount?: number;
    description?: string;
    friendlyUrl?: string;
    title?: string;
    documentId?: number;
    readonly id?: number;
    jsonProperty?: string;
    name?: string;
    nestedTestEntity?: NestedTestEntity;
    self?: string;
    testEntities?: Array<TestEntity>;
    type?: 'ChildTestEntity1' | 'ChildTestEntity2' | 'ChildTestEntity3';
};


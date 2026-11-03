/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ImportUpload, ImportUploadRequest} from '../types/importUpload';
import ApiHelper, {RequestResult} from './ApiHelper';

export function deleteImportUpload(
	importUploadURL: string
): Promise<RequestResult<void>> {
	return ApiHelper.delete(importUploadURL);
}

export function getImportUpload(
	importUploadURL: string
): Promise<RequestResult<ImportUpload>> {
	return ApiHelper.get<ImportUpload>(importUploadURL);
}

export function getImportUploadURL(
	importUploadAPIURL: string,
	importUploadId: string
) {
	const [importUploadsURL] = importUploadAPIURL.split('?');

	return `${importUploadsURL}/${importUploadId}`;
}

export function postImportUpload(
	importUploadsURL: string,
	importUploadRequest: ImportUploadRequest
): Promise<RequestResult<ImportUpload>> {
	return ApiHelper.post<ImportUpload>(importUploadsURL, importUploadRequest);
}

export function putImportUploadChunk(
	importUploadURL: string,
	chunkIndex: number,
	blob: Blob,
	onProgress?: (loadedBytes: number) => void,
	signal?: AbortSignal
): Promise<RequestResult<ImportUpload>> {
	return ApiHelper.putBlobWithProgress<ImportUpload>(
		`${importUploadURL}/chunks/${chunkIndex}`,
		blob,
		onProgress,
		signal
	);
}

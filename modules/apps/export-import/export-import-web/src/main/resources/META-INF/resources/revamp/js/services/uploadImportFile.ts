/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ImportPreview} from '../types/exportImportPreview';
import {ImportUpload, UploadProgress} from '../types/importUpload';
import {ABORTED_ERROR, RequestResult} from './ApiHelper';
import ImportUploadService from './ImportUploadService';

const ABORTED_RESULT = {data: null, error: ABORTED_ERROR} as const;

const MAX_RETRIES_COUNT = 8;

const MAX_RETRY_DELAY = 30000;

const POLLING_INTERVAL = 1000;

const RETRY_DELAY = 1000;

interface UploadImportFileResult {
	importPreview: ImportPreview;
	importUploadId: string;
}

async function getOrPostImportUpload(
	file: File,
	importUploadAPIURL: string,
	importUploadId?: string
): Promise<RequestResult<ImportUpload>> {
	if (importUploadId) {
		const importUpload = await getResumableImportUpload(
			file,
			importUploadAPIURL,
			importUploadId
		);

		if (importUpload) {
			return {data: importUpload, error: null};
		}
	}

	return ImportUploadService.postImportUpload(
		{
			contentType: file.type || 'application/zip',
			fileName: file.name,
			size: file.size,
		},
		importUploadAPIURL
	);
}

async function getResumableImportUpload(
	file: File,
	importUploadAPIURL: string,
	importUploadId: string
): Promise<ImportUpload | null> {
	const result = await ImportUploadService.getImportUpload(
		importUploadAPIURL,
		importUploadId
	);

	if (
		!result.data ||
		result.data.status === 'FAILED' ||
		result.data.fileName !== file.name ||
		result.data.size !== file.size
	) {
		return null;
	}

	return result.data;
}

function isRetryable(result: RequestResult<unknown>) {
	return (
		result.error !== ABORTED_ERROR &&
		(!result.status || Number(result.status) >= 500)
	);
}

async function requestWithRetries<T>(
	request: () => Promise<RequestResult<T>>,
	signal?: AbortSignal
): Promise<RequestResult<T>> {
	let result = await request();

	for (
		let retriesCount = 0;
		retriesCount < MAX_RETRIES_COUNT && result.error && isRetryable(result);
		retriesCount++
	) {
		await wait(
			Math.min(RETRY_DELAY * 2 ** retriesCount, MAX_RETRY_DELAY),
			signal
		);

		if (signal?.aborted) {
			return ABORTED_RESULT;
		}

		result = await request();
	}

	return result;
}

async function uploadChunks(
	file: File,
	importUpload: ImportUpload,
	importUploadAPIURL: string,
	onProgress: (progress: UploadProgress) => void,
	signal?: AbortSignal
): Promise<RequestResult<ImportUpload>> {
	let result: RequestResult<ImportUpload> = {data: importUpload, error: null};

	for (
		let chunkIndex = Math.min(
			importUpload.uploadedChunksCount,
			importUpload.totalChunksCount - 1
		);
		chunkIndex < importUpload.totalChunksCount;
		chunkIndex++
	) {
		const start = chunkIndex * importUpload.chunkSize;

		result = await requestWithRetries(
			() =>
				ImportUploadService.putImportUploadChunk(
					file.slice(start, start + importUpload.chunkSize),
					chunkIndex,
					importUploadAPIURL,
					importUpload.id,
					(loadedBytes) =>
						onProgress({
							percentage: Math.min(
								Math.floor(
									((start + loadedBytes) * 100) /
										Math.max(file.size, 1)
								),
								100
							),
							status: 'UPLOADING',
						}),
					signal
				),
			signal
		);

		if (result.error) {
			return result;
		}
	}

	return result;
}

function wait(delay: number, signal?: AbortSignal) {
	return new Promise<void>((resolve) => {
		const abortController = new AbortController();

		const timeout = setTimeout(() => {
			abortController.abort();

			resolve();
		}, delay);

		signal?.addEventListener(
			'abort',
			() => {
				clearTimeout(timeout);

				resolve();
			},
			{signal: abortController.signal}
		);
	});
}

async function waitForValidation(
	importUploadAPIURL: string,
	importUploadId: string,
	onProgress: (progress: UploadProgress) => void,
	signal?: AbortSignal
): Promise<RequestResult<ImportUpload>> {
	onProgress({percentage: 0, status: 'VALIDATING'});

	while (!signal?.aborted) {
		const result = await requestWithRetries(
			() =>
				ImportUploadService.getImportUpload(
					importUploadAPIURL,
					importUploadId
				),
			signal
		);

		if (signal?.aborted) {
			break;
		}

		if (!result.data) {
			return result;
		}

		const {errorMessage, percentage, status} = result.data;

		if (status === 'FAILED') {
			ImportUploadService.deleteImportUpload(
				importUploadAPIURL,
				importUploadId
			);

			return {
				data: null,
				error:
					errorMessage ||
					Liferay.Language.get('an-unexpected-error-occurred'),
			};
		}

		if (status === 'READY') {
			return result;
		}

		if (status === 'UPLOADING') {
			return {
				data: null,
				error: Liferay.Language.get(
					'the-upload-was-interrupted-select-the-same-file-again-to-resume-it'
				),
			};
		}

		onProgress({percentage: percentage ?? 0, status: 'VALIDATING'});

		await wait(POLLING_INTERVAL, signal);
	}

	return ABORTED_RESULT;
}

export async function uploadImportFile({
	file,
	importUploadAPIURL,
	importUploadId,
	onImportUploadId,
	onProgress,
	signal,
}: {
	file: File;
	importUploadAPIURL: string;
	importUploadId?: string;
	onImportUploadId?: (importUploadId: string) => void;
	onProgress: (progress: UploadProgress) => void;
	signal?: AbortSignal;
}): Promise<RequestResult<UploadImportFileResult>> {
	const importUploadResult = await getOrPostImportUpload(
		file,
		importUploadAPIURL,
		importUploadId
	);

	if (!importUploadResult.data) {
		return importUploadResult;
	}

	const importUpload = importUploadResult.data;

	onImportUploadId?.(importUpload.id);

	const handleAbort = () => {
		ImportUploadService.deleteImportUpload(
			importUploadAPIURL,
			importUpload.id
		);
	};

	if (signal?.aborted) {
		handleAbort();

		return ABORTED_RESULT;
	}

	signal?.addEventListener('abort', handleAbort, {once: true});

	try {
		if (importUpload.status === 'UPLOADING') {
			const uploadResult = await uploadChunks(
				file,
				importUpload,
				importUploadAPIURL,
				onProgress,
				signal
			);

			if (!uploadResult.data) {
				if (uploadResult.error === ABORTED_ERROR) {
					return uploadResult;
				}

				if (isRetryable(uploadResult)) {
					return {
						data: null,
						error: Liferay.Language.get(
							'the-upload-was-interrupted-select-the-same-file-again-to-resume-it'
						),
					};
				}

				ImportUploadService.deleteImportUpload(
					importUploadAPIURL,
					importUpload.id
				);

				return uploadResult;
			}
		}

		const result = await waitForValidation(
			importUploadAPIURL,
			importUpload.id,
			onProgress,
			signal
		);

		if (!result.data) {
			return result;
		}

		return {
			data: {
				importPreview: result.data.importPreview as ImportPreview,
				importUploadId: result.data.id,
			},
			error: null,
		};
	}
	finally {
		signal?.removeEventListener('abort', handleAbort);
	}
}

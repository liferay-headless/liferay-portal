/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ImportPreview} from '../types/exportImportPreview';
import {
	ImportProcess,
	ImportProcessRequest,
	InstanceImportProcessRequest,
} from '../types/exportImportProcess';
import ApiHelper from './ApiHelper';

interface ImportPreviewParams {
	file: File;
	onProgress: (percent: number) => void;
	signal?: AbortSignal;
	url: string;
}

interface ImportProcessParams {
	importProcessRequest: ImportProcessRequest | InstanceImportProcessRequest;
	url: string;
}

function postImportPreview({
	file,
	onProgress,
	signal,
	url,
}: ImportPreviewParams) {
	return ApiHelper.postFormDataWithProgress<ImportPreview>(
		url,
		Liferay.Util.objectToFormData({file}),
		onProgress,
		signal
	);
}

function postImportProcess({importProcessRequest, url}: ImportProcessParams) {
	return ApiHelper.post<ImportProcess>(url, importProcessRequest);
}

export default {
	postImportPreview,
	postImportProcess,
};

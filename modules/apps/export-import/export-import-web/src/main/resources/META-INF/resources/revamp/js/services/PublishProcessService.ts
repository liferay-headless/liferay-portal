/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {
	PublishProcess,
	PublishProcessRequest,
} from '../types/exportImportProcess';
import ApiHelper from './ApiHelper';

interface PublishProcessParams {
	publishProcessRequest: PublishProcessRequest;
	url: string;
}

function postPublishProcess({
	publishProcessRequest,
	url,
}: PublishProcessParams) {
	return ApiHelper.post<PublishProcess>(url, publishProcessRequest);
}

export default {
	postPublishProcess,
};

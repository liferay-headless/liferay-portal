/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {
	ScheduledPublishProcess,
	ScheduledPublishProcessRequest,
} from '../types/exportImportProcess';
import ApiHelper from './ApiHelper';

interface ScheduledPublishProcessParams {
	url: string;
}

interface ScheduledPublishProcessRequestParams {
	scheduledPublishProcessRequest: ScheduledPublishProcessRequest;
	url: string;
}

function deleteScheduledPublishProcess({url}: ScheduledPublishProcessParams) {
	return ApiHelper.delete(url);
}

function getScheduledPublishProcess({url}: ScheduledPublishProcessParams) {
	return ApiHelper.get<ScheduledPublishProcess>(url);
}

function postScheduledPublishProcess({
	scheduledPublishProcessRequest,
	url,
}: ScheduledPublishProcessRequestParams) {
	return ApiHelper.post<ScheduledPublishProcess>(
		url,
		scheduledPublishProcessRequest
	);
}

export default {
	deleteScheduledPublishProcess,
	getScheduledPublishProcess,
	postScheduledPublishProcess,
};

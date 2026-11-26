/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {addParams} from 'frontend-js-web';

import {Group, Preview} from '../types/exportImportPreview';
import {DateFilter} from '../types/exportImportProcess';
import ApiHelper from './ApiHelper';

interface GroupsPage {
	items: Group[];
	lastPage: number;
	page: number;
	pageSize: number;
	totalCount: number;
}

export interface PreviewParams {
	dateFilter?: DateFilter;
	url: string;
}

function getExportPreviewGroupsCount(url: string) {
	return ApiHelper.get<GroupsPage>(
		addParams({page: '1', pageSize: '1'}, url)
	);
}

function getPreview({dateFilter, url}: PreviewParams) {
	if (!dateFilter) {
		return ApiHelper.get<Preview>(url);
	}

	const {type, ...dateFilterParams} = dateFilter;

	const params: Record<string, string> = {dateFilterType: type};

	Object.entries(dateFilterParams).forEach(([key, value]) => {
		if (value !== undefined && value !== null && value !== '') {
			params[key] = String(value);
		}
	});

	return ApiHelper.get<Preview>(addParams(params, url));
}

export default {
	getExportPreviewGroupsCount,
	getPreview,
};

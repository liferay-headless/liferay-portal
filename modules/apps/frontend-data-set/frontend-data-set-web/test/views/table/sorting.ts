/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {TSort} from '../../../src/main/resources/META-INF/resources/utils/types';
import {
	getFieldSortKey,
	getTableSorting,
	getUpdatedSorts,
} from '../../../src/main/resources/META-INF/resources/views/table/sorting';

describe('getFieldSortKey', () => {
	it('prefers the sort field name when it is set', () => {
		expect(
			getFieldSortKey({
				fieldName: 'type.label',
				sortFieldName: 'type/label',
			})
		).toBe('type/label');
	});

	it('falls back to the field name when no sort field name is set', () => {
		expect(getFieldSortKey({fieldName: 'title'})).toBe('title');
	});

	it('stringifies an array field name to a comma-joined value', () => {
		expect(getFieldSortKey({fieldName: ['type', 'label']})).toBe(
			'type,label'
		);
	});
});

describe('getTableSorting', () => {
	it('maps an active sort keyed by the sort field name back to its column', () => {
		const sorts: TSort[] = [
			{active: false, direction: 'asc', key: 'title'},
			{active: true, direction: 'asc', key: 'type/label'},
		];
		const visibleFields = [
			{fieldName: 'title'},
			{fieldName: 'type.label', sortFieldName: 'type/label'},
		];

		expect(getTableSorting(sorts, visibleFields)).toEqual({
			column: 'type.label',
			direction: 'ascending',
		});
	});

	it('returns null when no sort is active', () => {
		const sorts: TSort[] = [{active: false, key: 'title'}];

		expect(getTableSorting(sorts, [{fieldName: 'title'}])).toBeNull();
	});
});

describe('getUpdatedSorts', () => {
	it('stores the sort field name as the key when a complex column is sorted', () => {
		const sorts: TSort[] = [
			{active: false, direction: 'asc', key: 'type/label', label: 'Type'},
		];
		const visibleFields = [
			{fieldName: 'type.label', sortFieldName: 'type/label'},
		];

		const updatedSorts = getUpdatedSorts(sorts, visibleFields, {
			column: 'type.label',
			direction: 'ascending',
		});

		const activeSort = updatedSorts.find((sort) => sort.active);

		expect(activeSort?.key).toBe('type/label');
		expect(activeSort?.direction).toBe('asc');
	});
});

/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import React from 'react';

import {TSort} from '../../utils/types';

type SortableField = {
	fieldName: string | string[];
	sortFieldName?: string;
};

export type Sorting = {
	column: React.Key;
	direction: 'ascending' | 'descending';
};

export function getFieldSortKey(field: SortableField): string {
	return field.sortFieldName ?? String(field.fieldName);
}

export function getTableSorting(
	sorts: TSort[],
	visibleFields: SortableField[]
): Sorting | null {
	const activeSort = sorts.find((sort) => sort.active);

	if (!activeSort) {
		return null;
	}

	const matchedField = visibleFields.find(
		(field) => getFieldSortKey(field) === activeSort.key
	);

	return {
		column: matchedField
			? String(matchedField.fieldName)
			: activeSort.key ?? '',
		direction: activeSort.direction === 'desc' ? 'descending' : 'ascending',
	};
}

export function getUpdatedSorts(
	sorts: TSort[],
	visibleFields: SortableField[],
	sorting: Sorting | null
): TSort[] {
	const matchedField = visibleFields.find(
		(field) => String(field.fieldName) === sorting?.column?.toString()
	);

	const sortKey = matchedField
		? getFieldSortKey(matchedField)
		: String(sorting?.column);

	const updatedSorts: TSort[] = sorts.map((sort) =>
		sort.key === sortKey
			? {
					...sort,
					active: true,
					direction:
						sorting?.direction === 'ascending' ? 'asc' : 'desc',
				}
			: {
					...sort,
					active: false,
				}
	);

	if (!sorts.find((sort) => sort.key === sortKey)) {
		updatedSorts.push({
			active: true,
			direction: 'asc',
			key: sortKey,
		});
	}

	return updatedSorts;
}

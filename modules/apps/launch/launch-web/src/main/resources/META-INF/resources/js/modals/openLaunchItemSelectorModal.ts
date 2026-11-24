/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {EConfigInURLBehavior} from '@liferay/frontend-data-set-web';
import {openItemSelectorModal} from '@liferay/frontend-js-item-selector-web';

import {Launch} from '../api/launches';
import LaunchStatusCell from '../renderers/LaunchStatusCell';

const LAUNCH_SETS_API_URL = '/o/launch-sets';

function getAPIURL(excludeLaunchIds: number[]): string {
	let filter = 'status in (2, 7)';

	if (excludeLaunchIds.length) {
		filter += ` and not (id in (${excludeLaunchIds
			.map((launchId) => `'${launchId}'`)
			.join(', ')}))`;
	}

	return `${LAUNCH_SETS_API_URL}?filter=${encodeURIComponent(filter)}`;
}

export default function openLaunchItemSelectorModal({
	excludeLaunchIds = [],
	onSelect,
}: {
	excludeLaunchIds?: number[];
	onSelect: (launch: Launch) => void;
}): void {
	openItemSelectorModal<Launch>({
		apiURL: getAPIURL(excludeLaunchIds),
		confirmButtonLabel: Liferay.Language.get('add-to-launch'),
		fdsProps: {
			configInURLBehavior: EConfigInURLBehavior.OFF,
			customRenderers: {
				tableCell: [
					{
						component: LaunchStatusCell,
						name: 'launchStatus',
						type: 'internal',
					},
				],
			},
			emptyState: {
				description: Liferay.Language.get(
					'create-a-launch-in-launches-to-add-items-to-it'
				),
				image: '/states/empty_state.svg',
				title: Liferay.Language.get('no-launches-available'),
			},
			id: 'launchItemSelector',
			pagination: {
				deltas: [{label: 20}, {label: 40}, {label: 60}],
				initialDelta: 20,
			},
			sorts: [
				{
					direction: 'asc',
					key: 'name',
					label: Liferay.Language.get('name'),
				},
				{
					direction: 'desc',
					key: 'dateModified',
					label: Liferay.Language.get('last-modified'),
				},
			],
			views: [
				{
					contentRenderer: 'table',
					label: Liferay.Language.get('table'),
					name: 'table',
					schema: {
						fields: [
							{
								fieldName: 'name',
								label: Liferay.Language.get('name'),
								sortable: true,
							},
							{
								fieldName: 'description',
								label: Liferay.Language.get('description'),
							},
							{
								contentRenderer: 'launchStatus',
								fieldName: 'status',
								label: Liferay.Language.get('status'),
							},
							{
								contentRenderer: 'dateTime',
								fieldName: 'scheduledDate',
								label: Liferay.Language.get('scheduled-date'),
							},
						],
					},
				},
			],
		},
		itemTypeLabel: Liferay.Language.get('launch'),
		items: [],
		locator: {
			id: 'id',
			label: 'name',
			value: 'id',
		},
		multiSelect: false,
		onItemsChange: (launches: Launch[]) => {
			if (launches.length) {
				onSelect(launches[0]);
			}
		},
		size: 'lg',
	});
}

/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayLabel from '@clayui/label';
import React from 'react';

import {LaunchEntry} from '../api/launches';
import useLaunchEntrySummary from '../hooks/useLaunchEntrySummary';

const WORKFLOW_STATUS_APPROVED = 0;
const WORKFLOW_STATUS_DENIED = 4;

interface Props {
	getLaunchEntrySummaryURL: string;
	itemData: LaunchEntry;
	portletNamespace: string;
	published: boolean;
}

export default function LaunchEntryStatusCell({
	getLaunchEntrySummaryURL,
	itemData,
	portletNamespace,
	published,
}: Props) {
	const launchEntrySummary = useLaunchEntrySummary(
		itemData,
		portletNamespace,
		getLaunchEntrySummaryURL,
		published
	);

	if (!launchEntrySummary) {
		return null;
	}

	if (launchEntrySummary.status === WORKFLOW_STATUS_DENIED) {
		return (
			<ClayLabel displayType="danger">
				{Liferay.Language.get('rejected')}
			</ClayLabel>
		);
	}

	if (launchEntrySummary.status === WORKFLOW_STATUS_APPROVED) {
		return (
			<ClayLabel displayType="success">
				{Liferay.Language.get('ready-to-publish')}
			</ClayLabel>
		);
	}

	return (
		<ClayLabel displayType="info">
			{Liferay.Language.get('pending')}
		</ClayLabel>
	);
}

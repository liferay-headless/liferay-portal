/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayLabel from '@clayui/label';
import React from 'react';

import {LaunchEntry} from '../api/launches';
import WorkflowStatusLabel from '../components/WorkflowStatusLabel';
import useLaunchEntrySummary from '../hooks/useLaunchEntrySummary';
import useLaunchSetErrors from '../hooks/useLaunchSetErrors';

interface Props {
	getLaunchEntrySummaryURL: string;
	itemData: LaunchEntry;
	launchSetId: number;
	portletNamespace: string;
}

export default function LaunchEntryStatusCell({
	getLaunchEntrySummaryURL,
	itemData,
	launchSetId,
	portletNamespace,
}: Props) {
	const launchEntrySummary = useLaunchEntrySummary(
		itemData,
		portletNamespace,
		getLaunchEntrySummaryURL
	);

	const launchSetError = useLaunchSetErrors(launchSetId).find(
		(error) => Number(error.launchEntryId) === itemData.id
	);

	if (launchSetError) {
		return (
			<ClayLabel displayType="danger" title={launchSetError.message}>
				{Liferay.Language.get('failed')}
			</ClayLabel>
		);
	}

	if (!launchEntrySummary) {
		return null;
	}

	return <WorkflowStatusLabel workflowStatus={launchEntrySummary.status} />;
}

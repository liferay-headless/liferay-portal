/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import React from 'react';

import {Launch, isPublished} from '../api/launches';
import StatusLabel from '../components/StatusLabel';
import {
	WORKFLOW_STATUS_DRAFT,
	WORKFLOW_STATUS_SCHEDULED,
} from '../components/WorkflowStatusLabel';

export default function LaunchStatusCell({itemData}: {itemData: Launch}) {
	if (isPublished(itemData)) {
		return (
			<StatusLabel displayType="success">
				{Liferay.Language.get('published')}
			</StatusLabel>
		);
	}

	if (
		itemData.scheduledDate &&
		itemData.status?.code === WORKFLOW_STATUS_DRAFT
	) {
		return (
			<StatusLabel displayType="danger">
				{Liferay.Language.get('failed')}
			</StatusLabel>
		);
	}

	if (itemData.status?.code === WORKFLOW_STATUS_SCHEDULED) {
		return (
			<StatusLabel displayType="info">
				{Liferay.Language.get('scheduled')}
			</StatusLabel>
		);
	}

	return (
		<StatusLabel displayType="info">
			{Liferay.Language.get('in-progress')}
		</StatusLabel>
	);
}

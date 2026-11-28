/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import React from 'react';

import {WORKFLOW_STATUS} from '../util/constants';
import StatusLabel from './StatusLabel';

const WORKFLOW_STATUS_LABELS: Record<
	number,
	{
		displayType: React.ComponentProps<typeof StatusLabel>['displayType'];
		label: string;
	}
> = {
	[WORKFLOW_STATUS.APPROVED]: {
		displayType: 'success',
		label: Liferay.Language.get('approved'),
	},
	[WORKFLOW_STATUS.DENIED]: {
		displayType: 'danger',
		label: Liferay.Language.get('denied'),
	},
	[WORKFLOW_STATUS.DRAFT]: {
		displayType: 'secondary',
		label: Liferay.Language.get('draft'),
	},
	[WORKFLOW_STATUS.EXPIRED]: {
		displayType: 'warning',
		label: Liferay.Language.get('expired'),
	},
	[WORKFLOW_STATUS.IN_TRASH]: {
		displayType: 'secondary',
		label: Liferay.Language.get('in-trash'),
	},
	[WORKFLOW_STATUS.INACTIVE]: {
		displayType: 'secondary',
		label: Liferay.Language.get('inactive'),
	},
	[WORKFLOW_STATUS.INCOMPLETE]: {
		displayType: 'secondary',
		label: Liferay.Language.get('pending-approval'),
	},
	[WORKFLOW_STATUS.PENDING]: {
		displayType: 'info',
		label: Liferay.Language.get('pending'),
	},
	[WORKFLOW_STATUS.SCHEDULED]: {
		displayType: 'info',
		label: Liferay.Language.get('scheduled'),
	},
};

export default function WorkflowStatusLabel({
	workflowStatus,
}: {
	workflowStatus: number;
}) {
	const workflowStatusLabel = WORKFLOW_STATUS_LABELS[workflowStatus];

	if (!workflowStatusLabel) {
		return null;
	}

	return (
		<StatusLabel displayType={workflowStatusLabel.displayType}>
			{workflowStatusLabel.label}
		</StatusLabel>
	);
}

/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

export const WORKFLOW_STATUS = {
	APPROVED: 0,
	DENIED: 4,
	DRAFT: 2,
	EXPIRED: 3,
	IN_TRASH: 8,
	INACTIVE: 5,
	INCOMPLETE: 6,
	PENDING: 1,
	SCHEDULED: 7,
} as const;

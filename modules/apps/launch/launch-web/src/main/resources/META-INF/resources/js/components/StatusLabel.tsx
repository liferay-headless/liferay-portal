/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayLabel from '@clayui/label';
import React from 'react';

export default function StatusLabel({
	children,
	displayType,
}: {
	children: React.ReactNode;
	displayType: React.ComponentProps<typeof ClayLabel>['displayType'];
}) {
	return (
		<ClayLabel className="text-nowrap" displayType={displayType}>
			{children}
		</ClayLabel>
	);
}

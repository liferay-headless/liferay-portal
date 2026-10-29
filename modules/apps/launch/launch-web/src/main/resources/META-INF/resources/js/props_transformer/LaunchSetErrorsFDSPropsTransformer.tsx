/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayLabel from '@clayui/label';
import React from 'react';

import {LaunchSetError} from '../api/launches';
import LaunchSetErrorEntryCell from '../renderers/LaunchSetErrorEntryCell';

interface Props {
	additionalProps: {getLaunchEntrySummaryURL: string};
	portletNamespace: string;
	[key: string]: unknown;
}

export default function LaunchSetErrorsFDSPropsTransformer({
	additionalProps,
	portletNamespace,
	...props
}: Props) {
	return {
		...props,
		additionalProps,
		customRenderers: {
			tableCell: [
				{
					component: (cellProps: {itemData: LaunchSetError}) => (
						<LaunchSetErrorEntryCell
							{...cellProps}
							getLaunchEntrySummaryURL={
								additionalProps.getLaunchEntrySummaryURL
							}
							portletNamespace={portletNamespace}
						/>
					),
					name: 'launchSetErrorEntry',
					type: 'internal',
				},
				{
					component: ({itemData}: {itemData: LaunchSetError}) =>
						itemData.severity === 'critical' ? (
							<ClayLabel displayType="danger">
								{Liferay.Language.get('critical')}
							</ClayLabel>
						) : (
							<ClayLabel displayType="warning">
								{Liferay.Language.get('error')}
							</ClayLabel>
						),
					name: 'launchSetErrorSeverity',
					type: 'internal',
				},
			],
		},
		portletNamespace,
	};
}

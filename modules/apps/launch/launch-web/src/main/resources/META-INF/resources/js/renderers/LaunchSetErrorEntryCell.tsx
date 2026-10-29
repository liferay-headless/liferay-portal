/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import React, {useEffect, useState} from 'react';

import {LaunchEntry, LaunchSetError, getLaunchEntry} from '../api/launches';
import LaunchEntryTitleCell from './LaunchEntryTitleCell';

interface Props {
	getLaunchEntrySummaryURL: string;
	itemData: LaunchSetError;
	portletNamespace: string;
}

export default function LaunchSetErrorEntryCell({
	getLaunchEntrySummaryURL,
	itemData,
	portletNamespace,
}: Props) {
	const [launchEntry, setLaunchEntry] = useState<LaunchEntry | null>(null);

	const launchEntryId = Number(itemData.launchEntryId);

	useEffect(() => {
		setLaunchEntry(null);

		if (!launchEntryId) {
			return;
		}

		let current = true;

		getLaunchEntry(launchEntryId)
			.then((entry) => {
				if (current) {
					setLaunchEntry(entry);
				}
			})
			.catch(() => {});

		return () => {
			current = false;
		};
	}, [launchEntryId]);

	if (!launchEntry) {
		return itemData.launchEntryTitle ? (
			<span>{itemData.launchEntryTitle}</span>
		) : (
			<span className="text-secondary">&mdash;</span>
		);
	}

	return (
		<LaunchEntryTitleCell
			getLaunchEntrySummaryURL={getLaunchEntrySummaryURL}
			itemData={launchEntry}
			portletNamespace={portletNamespace}
		/>
	);
}

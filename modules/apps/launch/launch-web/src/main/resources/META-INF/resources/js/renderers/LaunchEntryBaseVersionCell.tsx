/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayLink from '@clayui/link';
import {openToast} from 'frontend-js-components-web';
import React from 'react';

import {
	LaunchEntry,
	addLaunchVersion,
	getLaunchEntryVersions,
	updateLaunchEntry,
} from '../api/launches';
import useLaunchEntrySummary from '../hooks/useLaunchEntrySummary';
import openSelectLaunchEntryVersionModal from '../modals/openSelectLaunchEntryVersionModal';

interface Props {
	addLaunchVersionURL: string;
	getLaunchEntrySummaryURL: string;
	getLaunchEntryVersionsURL: string;
	itemData: LaunchEntry;
	loadData: () => void;
	portletNamespace: string;
	published: boolean;
}

export default function LaunchEntryBaseVersionCell({
	addLaunchVersionURL,
	getLaunchEntrySummaryURL,
	getLaunchEntryVersionsURL,
	itemData,
	loadData,
	portletNamespace,
	published,
}: Props) {
	const launchEntrySummary = useLaunchEntrySummary(
		{...itemData, classVersion: itemData.baseClassVersion},
		portletNamespace,
		getLaunchEntrySummaryURL,
		published
	);

	function handleClick(event: React.MouseEvent) {
		event.preventDefault();

		getLaunchEntryVersions({
			className: itemData.className,
			classPK: itemData.classPK,
			portletNamespace,
			resourceURL: getLaunchEntryVersionsURL,
		})
			.then((launchEntryVersions) =>
				openSelectLaunchEntryVersionModal({
					launchEntryVersions: launchEntryVersions.filter(
						(launchEntryVersion) =>
							launchEntryVersion.classVersion !==
							itemData.classVersion
					),
					onSelect: (baseClassVersion) =>
						addLaunchVersion({
							baseClassVersion,
							className: itemData.className,
							classPK: itemData.classPK,
							launchSetId:
								itemData.r_launchSetToLaunchEntries_c_launchSetId,
							portletNamespace,
							resourceURL: addLaunchVersionURL,
						})
							.then((launchVersion) =>
								updateLaunchEntry({
									baseClassVersion:
										launchVersion.baseClassVersion,
									classVersion: launchVersion.classVersion,
									id: itemData.id,
								})
							)
							.then(() => loadData())
							.catch((exception: Error) =>
								openToast({
									message: exception.message,
									type: 'danger',
								})
							),
				})
			)
			.catch((exception: Error) =>
				openToast({message: exception.message, type: 'danger'})
			);
	}

	if (!launchEntrySummary) {
		return <span className="text-secondary">&mdash;</span>;
	}

	if (published) {
		return <span>{launchEntrySummary.version}</span>;
	}

	return (
		<ClayLink href="#" onClick={handleClick}>
			{launchEntrySummary.version}
		</ClayLink>
	);
}

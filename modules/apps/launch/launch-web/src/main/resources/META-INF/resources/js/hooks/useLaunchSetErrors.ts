/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {useEffect, useState} from 'react';

import {LaunchSetError, listLaunchSetErrors} from '../api/launches';

const cache = new Map<number, Promise<LaunchSetError[]>>();

export default function useLaunchSetErrors(
	launchSetId: number
): LaunchSetError[] {
	const [launchSetErrors, setLaunchSetErrors] = useState<LaunchSetError[]>(
		[]
	);

	useEffect(() => {
		if (!cache.has(launchSetId)) {
			cache.set(
				launchSetId,
				listLaunchSetErrors(launchSetId).catch(() => [])
			);
		}

		let current = true;

		cache.get(launchSetId)!.then((errors) => {
			if (current) {
				setLaunchSetErrors(errors);
			}
		});

		return () => {
			current = false;
		};
	}, [launchSetId]);

	return launchSetErrors;
}

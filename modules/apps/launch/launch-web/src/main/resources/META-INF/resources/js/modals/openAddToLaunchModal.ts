/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {sub} from 'frontend-js-web';

import {
	Launch,
	LaunchEntry,
	createLaunchEntry,
	listLaunchIdsForAsset,
} from '../api/launches';
import {openErrorToast, openSuccessToast} from '../util/toasts';
import openLaunchItemSelectorModal from './openLaunchItemSelectorModal';

export interface AddToLaunchAsset {
	className: string;
	classPK: number;
	classVersion: string;
}

export interface AddToLaunchResult {
	launch: Launch;
	launchEntry: LaunchEntry;
}

let busy = false;

async function addToLaunch(
	asset: AddToLaunchAsset,
	launch: Launch,
	onAdd?: (result: AddToLaunchResult) => void
) {
	busy = true;

	try {
		const launchEntry = await createLaunchEntry({
			baseVersionKey: asset.classVersion,
			className: asset.className,
			classPK: asset.classPK,
			launchSetId: launch.id,
		});

		openSuccessToast(
			sub(Liferay.Language.get('the-item-was-added-to-x'), launch.name)
		);

		onAdd?.({launch, launchEntry});
	}
	catch (error) {
		openErrorToast((error as Error).message);
	}
	finally {
		busy = false;
	}
}

export default async function openAddToLaunchModal(
	asset: AddToLaunchAsset,
	{onAdd}: {onAdd?: (result: AddToLaunchResult) => void} = {}
): Promise<void> {
	if (busy) {
		return;
	}

	busy = true;

	try {
		const excludeLaunchIds = await listLaunchIdsForAsset(asset);

		openLaunchItemSelectorModal({
			excludeLaunchIds,
			onSelect: (launch) => addToLaunch(asset, launch, onAdd),
		});
	}
	catch (error) {
		openErrorToast((error as Error).message);
	}
	finally {
		busy = false;
	}
}

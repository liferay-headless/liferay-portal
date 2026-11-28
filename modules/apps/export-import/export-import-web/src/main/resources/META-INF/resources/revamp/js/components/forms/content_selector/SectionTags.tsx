/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayLabel from '@clayui/label';
import {sub} from 'frontend-js-web';
import React from 'react';

export default function SectionTags({
	additionCount,
	deletionCount,
	selectedAdditionCount,
	selectedDeletionCount,
	tag,
}: {
	additionCount?: number;
	deletionCount?: number;
	selectedAdditionCount?: number;
	selectedDeletionCount?: number;
	tag?: string;
}) {
	if (!additionCount && !deletionCount && !tag) {
		return null;
	}

	return (
		<div className="align-items-center c-gap-2 d-inline-flex ml-2">
			{tag && <ClayLabel displayType="info">{tag}</ClayLabel>}

			{!!additionCount && (
				<ClayLabel displayType="secondary">
					{getAdditionLabel(additionCount, selectedAdditionCount)}
				</ClayLabel>
			)}

			{!!deletionCount && (
				<ClayLabel displayType="warning">
					{getDeletionLabel(deletionCount, selectedDeletionCount)}
				</ClayLabel>
			)}
		</div>
	);
}

function getAdditionLabel(
	additionCount: number,
	selectedAdditionCount: number | undefined
) {
	if (selectedAdditionCount !== undefined) {
		return sub(
			Liferay.Language.get('x-of-x-items-selected'),
			selectedAdditionCount,
			additionCount
		);
	}

	if (additionCount === 1) {
		return sub(Liferay.Language.get('x-item'), additionCount);
	}

	return sub(Liferay.Language.get('x-items'), additionCount);
}

function getDeletionLabel(
	deletionCount: number,
	selectedDeletionCount: number | undefined
) {
	if (selectedDeletionCount !== undefined) {
		return sub(
			Liferay.Language.get('x-of-x-deletions-selected'),
			selectedDeletionCount,
			deletionCount
		);
	}

	if (deletionCount === 1) {
		return sub(Liferay.Language.get('x-deletion'), deletionCount);
	}

	return sub(Liferay.Language.get('x-deletions'), deletionCount);
}

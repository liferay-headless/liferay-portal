/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import {openModal} from 'frontend-js-components-web';
import {escapeHTML} from 'frontend-js-web';
import React from 'react';

import submitLaunchAction from '../util/submitLaunchAction';

interface Props {
	scheduled: boolean;
	unscheduleLaunchURL: string;
}

export default function LaunchUnscheduleButton({
	scheduled,
	unscheduleLaunchURL,
}: Props) {
	function handleClick() {
		openModal({
			bodyHTML: escapeHTML(
				Liferay.Language.get(
					'unschedule-the-launch-will-delete-the-scheduled-job-and-the-launch-will-be-moved-to-open.-do-you-want-to-proceed'
				)
			),
			buttons: [
				{
					displayType: 'secondary',
					label: Liferay.Language.get('cancel'),
					type: 'cancel',
				},
				{
					autoFocus: true,
					displayType: 'primary',
					label: Liferay.Language.get('confirm'),
					onClick: ({processClose}: {processClose: () => void}) => {
						processClose();

						submitLaunchAction(unscheduleLaunchURL);
					},
				},
			],
			role: 'alertdialog',
			title: Liferay.Language.get('unschedule'),
		});
	}

	if (!scheduled) {
		return null;
	}

	return (
		<ClayButton
			className="text-nowrap"
			displayType="secondary"
			onClick={handleClick}
			small
			type="button"
		>
			{Liferay.Language.get('unschedule')}
		</ClayButton>
	);
}

/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {openToast} from 'frontend-js-components-web';
import {escapeHTML, fetch} from 'frontend-js-web';

export default async function submitLaunchAction(
	url: string,
	body: FormData = new FormData()
) {
	try {
		const response = await fetch(url, {
			body,
			method: 'POST',
		});

		const {error, redirect, validationError} = await response.json();

		if (error || validationError) {
			openToast({
				message: escapeHTML(error || validationError),
				type: 'danger',
			});

			return;
		}

		window.location.href = redirect;
	}
	catch (exception) {
		openToast({
			message: escapeHTML((exception as Error).message),
			type: 'danger',
		});
	}
}

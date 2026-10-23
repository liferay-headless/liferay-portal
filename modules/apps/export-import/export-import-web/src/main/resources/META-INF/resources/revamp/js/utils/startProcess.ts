/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {FDS_EVENT} from '@liferay/frontend-data-set-web';
import {FormikHelpers} from 'formik';

import {RequestResult} from '../services/ApiHelper';
import {navigateToProcesses} from './navigateToProcesses';

function openErrorToast(message: string) {
	Liferay.Util.openToast({message, type: 'danger'});
}

export async function startProcess<T, Values>({
	fdsName,
	formikHelpers,
	message,
	request,
	url,
}: {
	fdsName: string;
	formikHelpers: FormikHelpers<Values>;
	message: string;
	request: Promise<RequestResult<T>>;
	url: string;
}) {
	if (!Liferay.SPA?.app?.canNavigate(url)) {
		const result = await request;

		if (result.error) {
			openErrorToast(result.error);

			return;
		}

		navigateToProcesses(formikHelpers, message, url);

		return;
	}

	navigateToProcesses(formikHelpers, message, url);

	const result = await request;

	if (result.error) {
		openErrorToast(result.error);

		return;
	}

	Liferay.fire(FDS_EVENT.UPDATE_DISPLAY, {id: fdsName});
}

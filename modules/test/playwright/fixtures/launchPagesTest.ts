/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {test} from '@playwright/test';

import {LaunchItemSelectorPage} from '../pages/launch-web/LaunchItemSelectorPage';
import {LaunchesPage} from '../pages/launch-web/LaunchesPage';

const launchPagesTest = test.extend<{
	launchItemSelectorPage: LaunchItemSelectorPage;
	launchesPage: LaunchesPage;
}>({
	launchItemSelectorPage: async ({page}, use) => {
		await use(new LaunchItemSelectorPage(page));
	},
	launchesPage: async ({page}, use) => {
		await use(new LaunchesPage(page));
	},
});

export {launchPagesTest};

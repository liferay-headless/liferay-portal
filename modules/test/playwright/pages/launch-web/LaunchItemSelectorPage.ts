/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Locator, Page, expect} from '@playwright/test';

import {waitForAlert} from '../../utils/waitForAlert';

export class LaunchItemSelectorPage {
	readonly page: Page;

	readonly addToLaunchButton: Locator;
	readonly dialog: Locator;
	readonly searchInput: Locator;

	constructor(page: Page) {
		this.page = page;

		this.dialog = page.getByRole('dialog');

		this.addToLaunchButton = this.dialog.getByRole('button', {
			name: 'Add to Launch',
		});
		this.searchInput = this.dialog.getByPlaceholder('Search');
	}

	async addToLaunch(launchName: string) {
		await this._row(launchName).getByRole('radio').check();

		await this.addToLaunchButton.click();

		await waitForAlert(
			this.page,
			`Success:The item was added to ${launchName}.`
		);
	}

	async expectLaunchHidden(launchName: string) {
		await expect(this._row(launchName)).toHaveCount(0);
	}

	async expectLaunchListed(launchName: string) {
		await expect(this._row(launchName)).toBeVisible();
	}

	async search(text: string) {
		await expect(this.dialog).toContainText('Select Launch');

		await this.searchInput.fill(text);

		await this.searchInput.press('Enter');

		await expect(this.dialog).toContainText('Found for');
	}

	_row(launchName: string): Locator {
		return this.dialog.getByRole('row').filter({hasText: launchName});
	}
}

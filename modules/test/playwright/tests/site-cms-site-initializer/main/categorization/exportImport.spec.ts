/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {featureFlagsTest} from '../../../../fixtures/featureFlagsTest';
import {loginTest} from '../../../../fixtures/loginTest';
import {cmsPagesTest} from '../fixtures/cmsPagesTest';

const test = mergeTests(
	cmsPagesTest,
	featureFlagsTest({
		'LPD-57655': {enabled: true},
	}),
	loginTest()
);

test(
	'The Vocabularies view exposes separate Export and Import items for Vocabularies and Tags',
	{tag: '@LPD-88927'},
	async ({page, vocabulariesPage}) => {
		await vocabulariesPage.goto();

		await page.getByRole('button', {name: 'More Actions'}).click();

		await expect(
			page.getByRole('menuitem', {
				exact: true,
				name: 'Vocabularies Export',
			})
		).toBeVisible();
		await expect(
			page.getByRole('menuitem', {
				exact: true,
				name: 'Vocabularies Import',
			})
		).toBeVisible();
		await expect(
			page.getByRole('menuitem', {exact: true, name: 'Tags Export'})
		).toBeVisible();
		await expect(
			page.getByRole('menuitem', {exact: true, name: 'Tags Import'})
		).toBeVisible();

		await expect(
			page.getByRole('menuitem', {
				exact: true,
				name: 'Export/Import Vocabularies',
			})
		).toBeHidden();
		await expect(
			page.getByRole('menuitem', {
				exact: true,
				name: 'Export/Import Tags',
			})
		).toBeHidden();
	}
);

test(
	'The Tags view exposes the same four separate Export and Import items',
	{tag: '@LPD-88927'},
	async ({page, tagsPage}) => {
		await tagsPage.goto();

		await page.getByRole('button', {name: 'More Actions'}).click();

		await expect(
			page.getByRole('menuitem', {
				exact: true,
				name: 'Vocabularies Export',
			})
		).toBeVisible();
		await expect(
			page.getByRole('menuitem', {
				exact: true,
				name: 'Vocabularies Import',
			})
		).toBeVisible();
		await expect(
			page.getByRole('menuitem', {exact: true, name: 'Tags Export'})
		).toBeVisible();
		await expect(
			page.getByRole('menuitem', {exact: true, name: 'Tags Import'})
		).toBeVisible();
	}
);

test(
	'Clicking Vocabularies Export navigates to the dedicated CMS Export/Import page without opening a modal',
	{tag: '@LPD-88927'},
	async ({page, vocabulariesPage}) => {
		await vocabulariesPage.goto();

		await page.getByRole('button', {name: 'More Actions'}).click();
		await page
			.getByRole('menuitem', {exact: true, name: 'Vocabularies Export'})
			.click();

		await expect(page).toHaveURL(/\/export-import/);
		await expect(page.locator('.modal-content')).toBeHidden();
	}
);

test(
	'The CMS Export/Import page shows its title next to the back link',
	{tag: '@LPD-115187'},
	async ({page, vocabulariesPage}) => {
		await vocabulariesPage.goto();

		await page.getByRole('button', {name: 'More Actions'}).click();
		await page
			.getByRole('menuitem', {exact: true, name: 'Vocabularies Export'})
			.click();

		await expect(page.getByRole('link', {name: 'Back'})).toBeVisible();
		await expect(
			page.getByRole('heading', {level: 1, name: 'Vocabularies Export'})
		).toBeVisible();
		await expect(
			page.locator('.sidebar-body .nav-link.active', {
				hasText: 'Categorization',
			})
		).toBeVisible();

		await page.getByRole('link', {name: 'Back'}).click();

		await page.getByRole('button', {name: 'More Actions'}).click();
		await page
			.getByRole('menuitem', {exact: true, name: 'Tags Import'})
			.click();

		await expect(
			page.getByRole('heading', {level: 1, name: 'Tags Import'})
		).toBeVisible();
	}
);

test(
	'Clicking Tags Import from the Tags view navigates to the dedicated CMS Export/Import page',
	{tag: '@LPD-88927'},
	async ({page, tagsPage}) => {
		await tagsPage.goto();

		await page.getByRole('button', {name: 'More Actions'}).click();
		await page
			.getByRole('menuitem', {exact: true, name: 'Tags Import'})
			.click();

		await expect(page).toHaveURL(/\/export-import/);
		await expect(page.locator('.modal-content')).toBeHidden();
	}
);

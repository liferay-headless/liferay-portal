/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {featureFlagsTest} from '../../../../fixtures/featureFlagsTest';
import {loginTest} from '../../../../fixtures/loginTest';
import {clickAndExpectToBeVisible} from '../../../../utils/clickAndExpectToBeVisible';
import getRandomString from '../../../../utils/getRandomString';
import {PORTLET_URLS} from '../../../../utils/portletUrls';
import {exportImportPagesTest} from '../../../export-import-web/revamp/fixtures/exportImportPagesTest';
import {cmsPagesTest} from '../fixtures/cmsPagesTest';

const test = mergeTests(
	cmsPagesTest,
	dataApiHelpersTest,
	exportImportPagesTest,
	featureFlagsTest({
		'LPD-57655': {enabled: true},
	}),
	loginTest()
);

test(
	'System folders L_CONTENTS and L_FILES are excluded from export results in a new CMS Space',
	{tag: '@LPD-83026'},
	async ({
		apiHelpers,
		exportImportDataSelectionPage,
		exportImportPage,
		homePage,
		page,
	}) => {
		const space = await apiHelpers.headlessAssetLibrary.createSpace();

		const userFolderName = `User Folder ${getRandomString()}`;

		await apiHelpers.objectFolder.createObjectEntryFolder({
			parentObjectEntryFolderExternalReferenceCode: 'L_CONTENTS',
			scopeKey: space.siteId,
			title: userFolderName,
		});

		await homePage.goto();

		await clickAndExpectToBeVisible({
			autoClick: true,
			target: page.getByRole('menuitem', {name: 'All Spaces'}),
			trigger: page.getByRole('menuitem', {exact: true, name: 'Spaces'}),
		});

		await page.getByRole('link', {exact: true, name: space.name}).click();

		await page.getByRole('button', {name: 'More Actions'}).waitFor();

		await clickAndExpectToBeVisible({
			autoClick: true,
			target: page.getByRole('menuitem', {name: 'Export'}),
			trigger: page.getByRole('button', {name: 'More Actions'}),
		});

		await exportImportPage.clickNew();

		const exportableItems =
			await exportImportDataSelectionPage.getExportableItems();

		expect(exportableItems.get('Object Entry Folders')).toBe(1);
	}
);

test(
	'Export and Import of a Space stay in the CMS and name the Space in the title',
	{tag: '@LPD-115187'},
	async ({apiHelpers, exportImportPage, page}) => {
		const space = await apiHelpers.headlessAssetLibrary.createSpace();

		for (const action of ['Export', 'Import']) {
			await test.step(`Open ${action} from All Spaces`, async () => {
				await page.goto(PORTLET_URLS.cmsAllSpaces);

				await clickAndExpectToBeVisible({
					autoClick: true,
					target: page.getByRole('menuitem', {
						exact: true,
						name: action,
					}),
					trigger: page.getByRole('button', {
						name: `${space.name} Actions`,
					}),
				});

				await expect(page).toHaveURL(/\/web\/cms\/export-import/);
				await expect(
					page.getByRole('heading', {
						exact: true,
						level: 1,
						name: `${space.name} ${action}`,
					})
				).toBeVisible();

				await page.getByRole('link', {name: 'Back'}).click();

				await expect(page).toHaveURL(/\/web\/cms\/all-spaces/);
			});

			await test.step(`Open ${action} from the Space`, async () => {
				await page
					.getByRole('link', {exact: true, name: space.name})
					.click();

				await page
					.getByRole('button', {name: 'More Actions'})
					.waitFor();

				await clickAndExpectToBeVisible({
					autoClick: true,
					target: page.getByRole('menuitem', {
						exact: true,
						name: action,
					}),
					trigger: page.getByRole('button', {name: 'More Actions'}),
				});

				await expect(page).toHaveURL(/\/web\/cms\/export-import/);
				await expect(
					page.getByRole('heading', {
						exact: true,
						level: 1,
						name: `${space.name} ${action}`,
					})
				).toBeVisible();
			});
		}

		await test.step('Open the new export wizard', async () => {
			await page.goto(PORTLET_URLS.cmsAllSpaces);

			await clickAndExpectToBeVisible({
				autoClick: true,
				target: page.getByRole('menuitem', {
					exact: true,
					name: 'Export',
				}),
				trigger: page.getByRole('button', {
					name: `${space.name} Actions`,
				}),
			});

			await exportImportPage.clickNew();

			await expect(page).toHaveURL(/\/web\/cms\/export-import/);
			await expect(
				page.getByRole('heading', {
					exact: true,
					level: 1,
					name: `New Export Process - ${space.name}`,
				})
			).toBeVisible();
		});
	}
);

/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Page, expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../../fixtures/apiHelpersTest';
import {featureFlagsTest} from '../../../fixtures/featureFlagsTest';
import {isolatedSiteTest} from '../../../fixtures/isolatedSiteTest';
import {launchPagesTest} from '../../../fixtures/launchPagesTest';
import {loginTest} from '../../../fixtures/loginTest';
import {WebContentPage} from '../../../pages/journal-web/WebContentPage';
import {clickAndExpectToBeVisible} from '../../../utils/clickAndExpectToBeVisible';
import getRandomString from '../../../utils/getRandomString';
import getBasicWebContentStructureId from '../../../utils/structured-content/getBasicWebContentStructureId';

const test = mergeTests(
	apiHelpersTest,
	featureFlagsTest({
		'LPD-72278': {enabled: true},
	}),
	isolatedSiteTest,
	launchPagesTest,
	loginTest()
).extend<{launchSetIds: number[]}>({
	launchSetIds: async ({apiHelpers}, use) => {
		const launchSetIds: number[] = [];

		await use(launchSetIds);

		for (const launchSetId of launchSetIds) {
			await apiHelpers.launch.deleteLaunchSet(launchSetId);
		}
	},
});

async function openLaunchItemSelectorFromHistory(
	page: Page,
	site: Site,
	title: string
) {
	await new WebContentPage(page).goto(site.friendlyUrlPath);

	await clickAndExpectToBeVisible({
		autoClick: true,
		target: page.getByRole('menuitem', {name: 'View History'}),
		trigger: page
			.locator('[data-qa-id="row"]')
			.filter({hasText: 'Approved'})
			.getByLabel(`Actions for ${title}`),
	});

	await clickAndExpectToBeVisible({
		autoClick: true,
		target: page.getByRole('menuitem', {name: 'Add to Launch'}),
		trigger: page
			.getByRole('row')
			.filter({hasText: title})
			.filter({hasText: 'Approved'})
			.getByRole('button', {name: 'Show Actions'}),
	});
}

test(
	'Adds an approved web content version to a launch from its history',
	{tag: ['@LPD-115341']},
	async ({apiHelpers, launchItemSelectorPage, launchSetIds, page, site}) => {
		const launchNamePrefix = getRandomString();

		const firstLaunchName = `${launchNamePrefix} First`;
		const publishedLaunchName = `${launchNamePrefix} Published`;
		const secondLaunchName = `${launchNamePrefix} Second`;

		const firstLaunchSet =
			await apiHelpers.launch.createLaunchSet(firstLaunchName);
		const publishedLaunchSet = await apiHelpers.objectEntry.postObjectEntry(
			{description: '', name: publishedLaunchName, status: {code: 0}},
			'launch-sets'
		);
		const secondLaunchSet =
			await apiHelpers.launch.createLaunchSet(secondLaunchName);

		launchSetIds.push(
			firstLaunchSet.id,
			publishedLaunchSet.id,
			secondLaunchSet.id
		);

		const title = getRandomString();

		await apiHelpers.jsonWebServicesJournal.addWebContent({
			ddmStructureId: await getBasicWebContentStructureId(apiHelpers),
			groupId: site.id,
			titleMap: {en_US: title},
		});

		await test.step('Add the version to a launch that is not published', async () => {
			await openLaunchItemSelectorFromHistory(page, site, title);

			await launchItemSelectorPage.search(launchNamePrefix);

			await launchItemSelectorPage.expectLaunchListed(secondLaunchName);
			await launchItemSelectorPage.expectLaunchHidden(
				publishedLaunchName
			);

			await launchItemSelectorPage.addToLaunch(firstLaunchName);

			expect(
				await apiHelpers.launch.getLaunchEntries(firstLaunchSet.id)
			).toHaveLength(1);
		});

		await test.step('Hide the launches that already contain the web content', async () => {
			await openLaunchItemSelectorFromHistory(page, site, title);

			await launchItemSelectorPage.search(launchNamePrefix);

			await launchItemSelectorPage.expectLaunchListed(secondLaunchName);
			await launchItemSelectorPage.expectLaunchHidden(firstLaunchName);
		});
	}
);

test(
	'Adds a draft web content to a launch from the web content list',
	{tag: ['@LPD-115341']},
	async ({apiHelpers, launchItemSelectorPage, launchSetIds, page, site}) => {
		const launchName = getRandomString();

		const launchSet = await apiHelpers.launch.createLaunchSet(launchName);

		launchSetIds.push(launchSet.id);

		const title = getRandomString();

		await apiHelpers.jsonWebServicesJournal.addWebContent({
			ddmStructureId: await getBasicWebContentStructureId(apiHelpers),
			groupId: site.id,
			serviceContext: {workflowAction: 2},
			titleMap: {en_US: title},
		});

		const webContentPage = new WebContentPage(page);

		await webContentPage.goto(site.friendlyUrlPath);

		await webContentPage.openAddToLaunch(title);

		await launchItemSelectorPage.search(launchName);

		await launchItemSelectorPage.addToLaunch(launchName);

		expect(
			await apiHelpers.launch.getLaunchEntries(launchSet.id)
		).toHaveLength(1);
	}
);

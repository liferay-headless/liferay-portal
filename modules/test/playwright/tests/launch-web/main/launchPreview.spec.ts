/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Page, expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../../fixtures/apiHelpersTest';
import {featureFlagsTest} from '../../../fixtures/featureFlagsTest';
import {fragmentsPagesTest} from '../../../fixtures/fragmentPagesTest';
import {isolatedSiteTest} from '../../../fixtures/isolatedSiteTest';
import {launchPagesTest} from '../../../fixtures/launchPagesTest';
import {loginTest} from '../../../fixtures/loginTest';
import {ApiHelpers} from '../../../helpers/ApiHelpers';
import {LaunchEntry, LaunchSet} from '../../../helpers/LaunchApiHelper';
import {LaunchPreviewPage} from '../../../pages/launch-web/LaunchPreviewPage';
import {LaunchesPage} from '../../../pages/launch-web/LaunchesPage';
import getRandomString from '../../../utils/getRandomString';
import getBasicWebContentStructureId from '../../../utils/structured-content/getBasicWebContentStructureId';
import getFragmentDefinition from '../../layout-content-page-editor-web/main/utils/getFragmentDefinition';
import getPageDefinition from '../../layout-content-page-editor-web/main/utils/getPageDefinition';
import {
	CAMPAIGN_LANDING_PAGE_TITLE,
	CHRISTMAS_BANNER_HTML,
	CHRISTMAS_BANNER_TEXT,
	CHRISTMAS_RIBBON_HTML,
	CHRISTMAS_RIBBON_TEXT,
	FOOTER_FRAGMENT,
	FOOTER_TEXT,
	FRAGMENT_SET_NAME,
	HALLOWEEN_BANNER_HTML,
	HALLOWEEN_BANNER_TEXT,
	HALLOWEEN_RIBBON_HTML,
	HALLOWEEN_RIBBON_TEXT,
	HEADER_FRAGMENT,
	HEADER_TEXT,
	LIVE_BANNER_TEXT,
	LIVE_RIBBON_TEXT,
	PROMO_RIBBON_FRAGMENT_NAME,
	PROMO_RIBBON_VERSIONS,
	SEASONAL_BANNER_TITLE,
	SEASONAL_BANNER_VERSIONS,
} from './constants/campaign';

const FRAGMENT_ENTRY_CLASS_NAME = 'com.liferay.fragment.model.FragmentEntry';

const JOURNAL_ARTICLE_CLASS_NAME = 'com.liferay.journal.model.JournalArticle';

const test = mergeTests(
	apiHelpersTest,
	featureFlagsTest({
		'LPD-72278': {enabled: true},
		'LPS-178052': {enabled: true},
	}),
	fragmentsPagesTest,
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

async function addCampaignSite(apiHelpers: ApiHelpers, site: Site) {
	const {fragmentCollectionId} =
		await apiHelpers.jsonWebServicesFragmentCollection.addFragmentCollection(
			{groupId: site.id, name: FRAGMENT_SET_NAME}
		);

	const headerFragment =
		await apiHelpers.jsonWebServicesFragmentEntry.addFragmentEntry({
			fragmentCollectionId,
			groupId: site.id,
			html: HEADER_FRAGMENT.html,
			name: HEADER_FRAGMENT.name,
		});

	const footerFragment =
		await apiHelpers.jsonWebServicesFragmentEntry.addFragmentEntry({
			fragmentCollectionId,
			groupId: site.id,
			html: FOOTER_FRAGMENT.html,
			name: FOOTER_FRAGMENT.name,
		});

	const [firstRibbonVersion, ...nextRibbonVersions] = PROMO_RIBBON_VERSIONS;

	const ribbonFragment =
		await apiHelpers.jsonWebServicesFragmentEntry.addFragmentEntry({
			fragmentCollectionId,
			groupId: site.id,
			html: firstRibbonVersion,
			name: PROMO_RIBBON_FRAGMENT_NAME,
		});

	for (const html of nextRibbonVersions) {
		await apiHelpers.jsonWebServicesFragmentEntry.updateFragmentEntry({
			fragmentCollectionId,
			fragmentEntryId: ribbonFragment.fragmentEntryId,
			html,
			name: PROMO_RIBBON_FRAGMENT_NAME,
		});
	}

	const [firstBannerVersion, ...nextBannerVersions] =
		SEASONAL_BANNER_VERSIONS;

	let banner = await apiHelpers.jsonWebServicesJournal.addWebContent({
		ddmStructureId: await getBasicWebContentStructureId(apiHelpers),
		descriptionMap: {en_US: ''},
		groupId: site.id,
		rawContent: firstBannerVersion,
		titleMap: {en_US: SEASONAL_BANNER_TITLE},
	});

	for (const html of nextBannerVersions) {
		banner = await apiHelpers.jsonWebServicesJournal.editWebContent(
			{description: '', rawContent: html, title: SEASONAL_BANNER_TITLE},
			site.id,
			banner
		);
	}

	const journalArticleClassName =
		await apiHelpers.jsonWebServicesClassName.fetchClassName(
			JOURNAL_ARTICLE_CLASS_NAME
		);

	const layout = await apiHelpers.headlessDelivery.createSitePage({
		pageDefinition: getPageDefinition([
			getFragmentDefinition({
				id: getRandomString(),
				key: headerFragment.fragmentEntryKey,
			}),
			getFragmentDefinition({
				id: getRandomString(),
				key: ribbonFragment.fragmentEntryKey,
			}),
			getFragmentDefinition({
				fragmentConfig: {
					itemSelector: {
						className: JOURNAL_ARTICLE_CLASS_NAME,
						classNameId: journalArticleClassName.classNameId,
						classPK: Number(banner.resourcePrimKey),
						title: SEASONAL_BANNER_TITLE,
					},
				},
				id: getRandomString(),
				key: 'com.liferay.fragment.internal.renderer.ContentObjectFragmentRenderer',
			}),
			getFragmentDefinition({
				id: getRandomString(),
				key: footerFragment.fragmentEntryKey,
			}),
		]),
		siteId: site.id,
		title: CAMPAIGN_LANDING_PAGE_TITLE,
	});

	return {
		banner,
		layout,
		ribbonFragment,
	};
}

function getURLParameter(url: string, name: string) {
	const {searchParams} = new URL(url);

	for (const [key, value] of searchParams) {
		if (key.endsWith(name)) {
			return value;
		}
	}

	return null;
}

async function expectEntryRow(
	launchesPage: LaunchesPage,
	{
		baseVersion,
		published = false,
		status,
		title,
		type,
		version,
	}: {
		baseVersion: string;
		published?: boolean;
		status: string;
		title: string;
		type: string;
		version: string;
	}
) {
	const row = launchesPage.getEntryRow(title);

	if (published) {
		await expect(row.getByRole('link')).toHaveCount(0);

		await expect(row).toContainText(title);
		await expect(row).toContainText(baseVersion);
	}
	else {
		await expect(
			row.getByRole('link', {exact: true, name: title})
		).toBeVisible();
		await expect(
			row.getByRole('link', {exact: true, name: baseVersion})
		).toBeVisible();
	}

	await expect(row.getByRole('cell').nth(1)).toHaveText(version);
	await expect(row).toContainText(type);
	await expect(row).toContainText(status);
}

async function expectPreview(
	previewPage: Page,
	{
		hidden,
		launchName,
		shown,
	}: {hidden: string[]; launchName: string; shown: string[]}
) {
	const launchPreviewPage = new LaunchPreviewPage(previewPage);

	await expect(launchPreviewPage.topbar).toContainText('Previewing');
	await expect(launchPreviewPage.getLaunchPicker(launchName)).toBeVisible();

	await expect(previewPage.getByText(HEADER_TEXT)).toBeVisible();
	await expect(previewPage.getByText(FOOTER_TEXT)).toBeVisible();

	for (const text of shown) {
		await expect(previewPage.getByText(text)).toBeVisible();
	}

	for (const text of hidden) {
		await expect(previewPage.getByText(text)).toBeHidden();
	}
}

test(
	'Previews the Christmas and Halloween campaigns with their own ribbon and banner over the live site',
	{tag: ['@LPD-72339']},
	async ({
		apiHelpers,
		fragmentEditorPage,
		launchSetIds,
		launchesPage,
		page,
		site,
	}) => {
		const {banner, layout, ribbonFragment} = await addCampaignSite(
			apiHelpers,
			site
		);

		const bannerResourcePrimKey = Number(banner.resourcePrimKey);

		async function updateBannerDraft(html: string) {
			await apiHelpers.jsonWebServicesJournal.editWebContent(
				{
					description: '',
					rawContent: html,
					title: SEASONAL_BANNER_TITLE,
				},
				site.id,
				{
					...banner,
					articleId: getURLParameter(page.url(), 'articleId'),
					serviceContext: {workflowAction: 2},
					version: getURLParameter(page.url(), 'version'),
				}
			);
		}

		const suffix = getRandomString().slice(0, 8);

		const christmasName = `Christmas Campaign ${suffix}`;
		const halloweenName = `Halloween Campaign ${suffix}`;

		const backToLaunches = page
			.locator('.control-menu-nav-item')
			.getByTitle('Launches');

		let christmas: LaunchSet;
		let christmasEntriesURL: string;
		let christmasRibbon: LaunchEntry;
		let halloween: LaunchSet;

		await test.step('Create the Christmas launch', async () => {
			await launchesPage.goto(site.friendlyUrlPath);

			await launchesPage.createLaunch(christmasName, 'December go-live');

			christmas =
				await apiHelpers.launch.getLaunchSetByName(christmasName);

			launchSetIds.push(christmas.id);

			await launchesPage.goToDetails();

			await expect(launchesPage.nameInput).toHaveValue(christmasName);
			await expect(launchesPage.descriptionInput).toHaveValue(
				'December go-live'
			);
		});

		await test.step('Add the ribbon and the banner to Christmas, each branched from the live version', async () => {
			await launchesPage.goToEntries();

			christmasEntriesURL = page.url();

			await launchesPage.addFragmentEntry(
				site.name,
				FRAGMENT_SET_NAME,
				PROMO_RIBBON_FRAGMENT_NAME
			);

			christmasRibbon = await apiHelpers.launch.getLaunchEntry(
				christmas.id,
				FRAGMENT_ENTRY_CLASS_NAME
			);

			expect(christmasRibbon.classPK).toBe(
				Number(ribbonFragment.fragmentEntryId)
			);
			expect(christmasRibbon.baseClassVersion).toBe(
				String(PROMO_RIBBON_VERSIONS.length)
			);
			expect(christmasRibbon.classVersion).not.toBe(
				ribbonFragment.fragmentEntryId
			);

			await expectEntryRow(launchesPage, {
				baseVersion: String(PROMO_RIBBON_VERSIONS.length),
				status: 'Draft',
				title: PROMO_RIBBON_FRAGMENT_NAME,
				type: 'Fragment',
				version: 'Draft',
			});

			await launchesPage.addWebContentEntry(
				site.name,
				SEASONAL_BANNER_TITLE
			);

			const christmasBanner = await apiHelpers.launch.getLaunchEntry(
				christmas.id,
				JOURNAL_ARTICLE_CLASS_NAME
			);

			expect(christmasBanner.classPK).toBe(bannerResourcePrimKey);
			expect(christmasBanner.baseClassVersion).toBe('1.2');
			expect(christmasBanner.classVersion).not.toBe(
				christmasBanner.baseClassVersion
			);

			await expectEntryRow(launchesPage, {
				baseVersion: '1.2',
				status: 'Draft',
				title: SEASONAL_BANNER_TITLE,
				type: 'Basic Web Content',
				version: 'Draft',
			});
		});

		await test.step('Redress the Christmas ribbon draft in the fragment editor', async () => {
			await launchesPage
				.getEntryRow(PROMO_RIBBON_FRAGMENT_NAME)
				.getByRole('link', {
					exact: true,
					name: PROMO_RIBBON_FRAGMENT_NAME,
				})
				.click();

			await expect(backToLaunches).toBeVisible();

			await fragmentEditorPage.addHTML(CHRISTMAS_RIBBON_HTML);

			await backToLaunches.click();

			await expect(launchesPage.newEntryButton).toBeVisible();
		});

		await test.step('Open the Christmas banner draft on its own version and redress it', async () => {
			await launchesPage
				.getEntryRow(SEASONAL_BANNER_TITLE)
				.getByRole('link', {exact: true, name: SEASONAL_BANNER_TITLE})
				.click();

			await expect(backToLaunches).toBeVisible();

			expect(getURLParameter(page.url(), 'articleId')).not.toBe(
				banner.articleId
			);

			await updateBannerDraft(CHRISTMAS_BANNER_HTML);

			await backToLaunches.click();

			await expect(launchesPage.newEntryButton).toBeVisible();

			await expectEntryRow(launchesPage, {
				baseVersion: '1.2',
				status: 'Draft',
				title: SEASONAL_BANNER_TITLE,
				type: 'Basic Web Content',
				version: 'Draft',
			});
		});

		await test.step('Create the Halloween launch with the same two items', async () => {
			await launchesPage.goto(site.friendlyUrlPath);

			await launchesPage.createLaunch(
				halloweenName,
				'31 October go-live'
			);

			halloween =
				await apiHelpers.launch.getLaunchSetByName(halloweenName);

			launchSetIds.push(halloween.id);

			await launchesPage.goToEntries();

			await launchesPage.addFragmentEntry(
				site.name,
				FRAGMENT_SET_NAME,
				PROMO_RIBBON_FRAGMENT_NAME
			);

			const halloweenRibbon = await apiHelpers.launch.getLaunchEntry(
				halloween.id,
				FRAGMENT_ENTRY_CLASS_NAME
			);

			expect(halloweenRibbon.classVersion).not.toBe(
				christmasRibbon.classVersion
			);

			await expectEntryRow(launchesPage, {
				baseVersion: String(PROMO_RIBBON_VERSIONS.length),
				status: 'Draft',
				title: PROMO_RIBBON_FRAGMENT_NAME,
				type: 'Fragment',
				version: 'Draft',
			});

			await launchesPage.addWebContentEntry(
				site.name,
				SEASONAL_BANNER_TITLE
			);

			await expectEntryRow(launchesPage, {
				baseVersion: '1.2',
				status: 'Draft',
				title: SEASONAL_BANNER_TITLE,
				type: 'Basic Web Content',
				version: 'Draft',
			});
		});

		await test.step('Pick versions for the Halloween items, which branches them again', async () => {
			const halloweenRibbon = await apiHelpers.launch.getLaunchEntry(
				halloween.id,
				FRAGMENT_ENTRY_CLASS_NAME
			);

			await launchesPage.selectEntryVersion(
				PROMO_RIBBON_FRAGMENT_NAME,
				halloweenRibbon.baseClassVersion,
				'3'
			);

			const rebranchedHalloweenRibbon =
				await apiHelpers.launch.getLaunchEntry(
					halloween.id,
					FRAGMENT_ENTRY_CLASS_NAME
				);

			expect(rebranchedHalloweenRibbon.baseClassVersion).toBe('3');
			expect(rebranchedHalloweenRibbon.classVersion).not.toBe(
				halloweenRibbon.classVersion
			);

			await expectEntryRow(launchesPage, {
				baseVersion: '3',
				status: 'Draft',
				title: PROMO_RIBBON_FRAGMENT_NAME,
				type: 'Fragment',
				version: 'Draft',
			});

			await launchesPage.selectEntryVersion(
				SEASONAL_BANNER_TITLE,
				'1.2',
				'1.2'
			);

			await expectEntryRow(launchesPage, {
				baseVersion: '1.2',
				status: 'Draft',
				title: SEASONAL_BANNER_TITLE,
				type: 'Basic Web Content',
				version: 'Draft',
			});
		});

		await test.step('Redress the Halloween ribbon draft in the fragment editor', async () => {
			await launchesPage
				.getEntryRow(PROMO_RIBBON_FRAGMENT_NAME)
				.getByRole('link', {
					exact: true,
					name: PROMO_RIBBON_FRAGMENT_NAME,
				})
				.click();

			await expect(backToLaunches).toBeVisible();

			await fragmentEditorPage.addHTML(HALLOWEEN_RIBBON_HTML);

			await backToLaunches.click();

			await expect(launchesPage.newEntryButton).toBeVisible();
		});

		await test.step('Redress the Halloween banner draft on its own version', async () => {
			await launchesPage
				.getEntryRow(SEASONAL_BANNER_TITLE)
				.getByRole('link', {exact: true, name: SEASONAL_BANNER_TITLE})
				.click();

			await expect(backToLaunches).toBeVisible();

			expect(getURLParameter(page.url(), 'articleId')).not.toBe(
				banner.articleId
			);

			await updateBannerDraft(HALLOWEEN_BANNER_HTML);

			await backToLaunches.click();

			await expect(launchesPage.newEntryButton).toBeVisible();

			await expectEntryRow(launchesPage, {
				baseVersion: '1.2',
				status: 'Draft',
				title: SEASONAL_BANNER_TITLE,
				type: 'Basic Web Content',
				version: 'Draft',
			});
		});

		await test.step('Christmas kept its own drafts', async () => {
			await page.goto(christmasEntriesURL);

			await expectEntryRow(launchesPage, {
				baseVersion: String(PROMO_RIBBON_VERSIONS.length),
				status: 'Draft',
				title: PROMO_RIBBON_FRAGMENT_NAME,
				type: 'Fragment',
				version: 'Draft',
			});

			await expectEntryRow(launchesPage, {
				baseVersion: '1.2',
				status: 'Draft',
				title: SEASONAL_BANNER_TITLE,
				type: 'Basic Web Content',
				version: 'Draft',
			});
		});

		let previewPage: Page;

		await test.step('Preview the Christmas launch on the landing page', async () => {
			previewPage = await launchesPage.previewOnPage(
				CAMPAIGN_LANDING_PAGE_TITLE
			);

			expect(previewPage.url()).toContain(
				`previewLaunchSetId=${christmas.id}`
			);
			expect(previewPage.url()).toContain('p_l_mode=preview');

			await expectPreview(previewPage, {
				hidden: [
					LIVE_RIBBON_TEXT,
					LIVE_BANNER_TEXT,
					HALLOWEEN_RIBBON_TEXT,
					HALLOWEEN_BANNER_TEXT,
				],
				launchName: christmasName,
				shown: [CHRISTMAS_RIBBON_TEXT, CHRISTMAS_BANNER_TEXT],
			});
		});

		await test.step('Switch the preview to the Halloween launch', async () => {
			const launchPreviewPage = new LaunchPreviewPage(previewPage);

			await launchPreviewPage.switchLaunch(christmasName, halloweenName);

			expect(previewPage.url()).toContain(
				`previewLaunchSetId=${halloween.id}`
			);

			await expectPreview(previewPage, {
				hidden: [
					CHRISTMAS_RIBBON_TEXT,
					CHRISTMAS_BANNER_TEXT,
					LIVE_RIBBON_TEXT,
					LIVE_BANNER_TEXT,
				],
				launchName: halloweenName,
				shown: [HALLOWEEN_RIBBON_TEXT, HALLOWEEN_BANNER_TEXT],
			});
		});

		await test.step('Switch the preview to the live site without leaving preview mode', async () => {
			const launchPreviewPage = new LaunchPreviewPage(previewPage);

			await launchPreviewPage.switchLaunch(halloweenName, 'No Launch');

			expect(previewPage.url()).toContain('previewLaunchSetId=0');

			await expectPreview(previewPage, {
				hidden: [
					CHRISTMAS_RIBBON_TEXT,
					CHRISTMAS_BANNER_TEXT,
					HALLOWEEN_RIBBON_TEXT,
					HALLOWEEN_BANNER_TEXT,
				],
				launchName: 'No Launch',
				shown: [LIVE_RIBBON_TEXT, LIVE_BANNER_TEXT],
			});

			await previewPage.close();
		});

		await test.step('Visitors of the live page see none of it', async () => {
			await page.goto(
				`/web${site.friendlyUrlPath}${layout.friendlyUrlPath}`
			);

			await expect(
				page.getByRole('navigation', {name: 'Preview'})
			).toBeHidden();

			await expect(page.getByText(LIVE_RIBBON_TEXT)).toBeVisible();
			await expect(page.getByText(LIVE_BANNER_TEXT)).toBeVisible();
			await expect(page.getByText(CHRISTMAS_RIBBON_TEXT)).toBeHidden();
			await expect(page.getByText(CHRISTMAS_BANNER_TEXT)).toBeHidden();
			await expect(page.getByText(HALLOWEEN_RIBBON_TEXT)).toBeHidden();
			await expect(page.getByText(HALLOWEEN_BANNER_TEXT)).toBeHidden();
		});

		await test.step('Publish the Christmas launch, so every entry it holds becomes the live version', async () => {
			await page.goto(christmasEntriesURL);

			await launchesPage.publishLaunch();

			const publishedRibbon = await apiHelpers.launch.getLaunchEntry(
				christmas.id,
				FRAGMENT_ENTRY_CLASS_NAME
			);

			expect(publishedRibbon.classVersion).toBe(
				String(PROMO_RIBBON_VERSIONS.length + 1)
			);

			const ribbon =
				await apiHelpers.jsonWebServicesFragmentEntry.fetchFragmentEntry(
					String(ribbonFragment.fragmentEntryId)
				);

			expect(ribbon.html).toContain(CHRISTMAS_RIBBON_TEXT);

			const publishedBanner = await apiHelpers.launch.getLaunchEntry(
				christmas.id,
				JOURNAL_ARTICLE_CLASS_NAME
			);

			expect(publishedBanner.classVersion).toBe('1.3');

			await expectEntryRow(launchesPage, {
				baseVersion: String(PROMO_RIBBON_VERSIONS.length),
				published: true,
				status: 'Approved',
				title: PROMO_RIBBON_FRAGMENT_NAME,
				type: 'Fragment',
				version: publishedRibbon.classVersion,
			});

			await expectEntryRow(launchesPage, {
				baseVersion: '1.2',
				published: true,
				status: 'Approved',
				title: SEASONAL_BANNER_TITLE,
				type: 'Basic Web Content',
				version: '1.3',
			});

			await page.goto(
				`/web${site.friendlyUrlPath}${layout.friendlyUrlPath}`
			);

			await expect(page.getByText(CHRISTMAS_RIBBON_TEXT)).toBeVisible();
			await expect(page.getByText(CHRISTMAS_BANNER_TEXT)).toBeVisible();
			await expect(page.getByText(LIVE_RIBBON_TEXT)).toBeHidden();
			await expect(page.getByText(LIVE_BANNER_TEXT)).toBeHidden();
			await expect(page.getByText(HALLOWEEN_RIBBON_TEXT)).toBeHidden();
			await expect(page.getByText(HALLOWEEN_BANNER_TEXT)).toBeHidden();
		});

		await test.step('The published launch is frozen, and offers nothing to change', async () => {
			await page.goto(christmasEntriesURL);

			await expect(launchesPage.publishedButton).toBeDisabled();
			await expect(launchesPage.newEntryButton).toBeHidden();

			const row = launchesPage.getEntryRow(PROMO_RIBBON_FRAGMENT_NAME);

			await expect(
				row.getByRole('link', {
					exact: true,
					name: PROMO_RIBBON_FRAGMENT_NAME,
				})
			).toBeHidden();
			await expect(
				row.getByRole('link', {
					exact: true,
					name: String(PROMO_RIBBON_VERSIONS.length),
				})
			).toBeHidden();
			await expect(
				row.getByRole('button', {name: 'Actions'})
			).toBeHidden();

			await launchesPage.goto(site.friendlyUrlPath);

			await expect(launchesPage.getEntryRow(christmasName)).toContainText(
				'Published'
			);
			await expect(launchesPage.getEntryRow(halloweenName)).toContainText(
				'In Progress'
			);
		});

		await test.step('Publishing Halloween over it leaves Christmas holding its own versions', async () => {
			await launchesPage.openLaunch(halloweenName);

			await launchesPage.goToEntries();

			await launchesPage.publishLaunch();

			await page.goto(
				`/web${site.friendlyUrlPath}${layout.friendlyUrlPath}`
			);

			await expect(page.getByText(HALLOWEEN_RIBBON_TEXT)).toBeVisible();
			await expect(page.getByText(HALLOWEEN_BANNER_TEXT)).toBeVisible();
			await expect(page.getByText(CHRISTMAS_RIBBON_TEXT)).toBeHidden();
			await expect(page.getByText(CHRISTMAS_BANNER_TEXT)).toBeHidden();

			await page.goto(christmasEntriesURL);

			await expectEntryRow(launchesPage, {
				baseVersion: String(PROMO_RIBBON_VERSIONS.length),
				published: true,
				status: 'Approved',
				title: PROMO_RIBBON_FRAGMENT_NAME,
				type: 'Fragment',
				version: String(PROMO_RIBBON_VERSIONS.length + 1),
			});

			await expectEntryRow(launchesPage, {
				baseVersion: '1.2',
				published: true,
				status: 'Approved',
				title: SEASONAL_BANNER_TITLE,
				type: 'Basic Web Content',
				version: '1.3',
			});
		});

		await test.step('Christmas is previewed as the state it holds, whatever the site serves now', async () => {
			previewPage = await launchesPage.previewOnPage(
				CAMPAIGN_LANDING_PAGE_TITLE
			);

			await expectPreview(previewPage, {
				hidden: [
					HALLOWEEN_RIBBON_TEXT,
					HALLOWEEN_BANNER_TEXT,
					LIVE_RIBBON_TEXT,
					LIVE_BANNER_TEXT,
				],
				launchName: christmasName,
				shown: [CHRISTMAS_RIBBON_TEXT, CHRISTMAS_BANNER_TEXT],
			});

			await previewPage.close();
		});
	}
);

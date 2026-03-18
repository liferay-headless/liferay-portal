/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';
import {createReadStream} from 'fs';
import path from 'path';

import {apiHelpersTest} from '../../../fixtures/apiHelpersTest';
import {applicationsMenuPageTest} from '../../../fixtures/applicationsMenuPageTest';
import {dataApiHelpersTest} from '../../../fixtures/dataApiHelpersTest';
import {featureFlagsTest} from '../../../fixtures/featureFlagsTest';
import {isolatedSiteTest} from '../../../fixtures/isolatedSiteTest';
import {loginTest} from '../../../fixtures/loginTest';
import {pageEditorPagesTest} from '../../../fixtures/pageEditorPagesTest';
import {portalLogCheckerTest} from '../../../fixtures/portalLogCheckerTest';
import {productMenuPageTest} from '../../../fixtures/productMenuPageTest';
import {systemSettingsPageTest} from '../../../fixtures/systemSettingsPageTest';
import {webContentDisplayPageTest} from '../../../fixtures/webContentDisplayPageTest';
import getRandomString from '../../../utils/getRandomString';
import getBasicWebContentStructureId from '../../../utils/structured-content/getBasicWebContentStructureId';
import {journalPagesTest} from '../../journal-web/main/fixtures/journalPagesTest';
import getDataStructureDefinition from '../../journal-web/main/utils/getDataStructureDefinition';
import {exportImportPagesTest} from './fixtures/exportImportPagesTest';

const test = mergeTests(
	applicationsMenuPageTest,
	apiHelpersTest,
	exportImportPagesTest,
	loginTest(),
	systemSettingsPageTest,
	pageEditorPagesTest,
	productMenuPageTest,
	webContentDisplayPageTest,
	isolatedSiteTest,
	dataApiHelpersTest,
	journalPagesTest,
	featureFlagsTest({
		'LPS-178052': {enabled: true},
	}),
	portalLogCheckerTest
);

test.describe('Portlet Export and Import', () => {
	test('Can import journal article when uncheck missing references validation', async ({
		apiHelpers,
		journalEditArticlePage,
		page,
		productMenuPage,
		site,
		systemSettingsPage,
		webContentDisplayPage,
	}) => {
		const webContentTitle = 'Web Content Title';
		const site2Erc = getRandomString();
		let filePath;
		let site2;

		await test.step('Given: Uncheck the Validate Missing References box in System settings', async () => {
			await systemSettingsPage.goToSystemSetting(
				'Infrastructure',
				'Export/Import, Staging'
			);
			await systemSettingsPage.checkOption(
				'Validate Missing References',
				false
			);
			await systemSettingsPage.saveAndWaitForAlert();
		});

		await test.step('And: Add a new web content with a link to a page', async () => {
			await apiHelpers.headlessDelivery.createSitePage({
				siteId: site.id,
				title: 'Test page',
			});

			await page.goto(`/web/${site.name}`);
			await productMenuPage.goToPages();
			await page.getByText('Test Page').click();

			const basicWebContentStructureId =
				await getBasicWebContentStructureId(apiHelpers);

			await apiHelpers.jsonWebServicesJournal.addWebContent({
				content: `<a href="/web${site.friendlyUrlPath}/test-page">test page</a>`,
				ddmStructureId: basicWebContentStructureId,
				groupId: site.id,
				titleMap: {en_US: webContentTitle},
			});
		});

		await test.step('When: Export the web content to a LAR file', async () => {
			await webContentDisplayPage.gotoWebContentAdmin(site.name);
			await journalEditArticlePage.selectExportImportOption();
			filePath = await journalEditArticlePage.export();
		});

		await test.step('And: Delete the site and create a new one', async () => {
			await apiHelpers.headlessSite.deleteSite(site.id);

			site2 = await apiHelpers.headlessSite.createSite({
				externalReferenceCode: site2Erc,
				name: site2Erc,
			});

			apiHelpers.data.push({id: site2.id, type: 'site'});
		});

		await test.step('And: Import the LAR file to this site', async () => {
			await webContentDisplayPage.gotoWebContentAdmin(site2.name);
			await journalEditArticlePage.selectExportImportOption();
			await journalEditArticlePage.import({filePath});
		});

		await test.step('Then: The web content will be present in the new site', async () => {
			await webContentDisplayPage.gotoWebContentAdmin(site2.name);
			await page.getByRole('link', {name: webContentTitle}).click();

			await expect(
				journalEditArticlePage.contentFrame.getByRole('link', {
					name: 'test page',
				})
			).toBeVisible();
		});
	});

	test('Export web content with deleted reference', async ({
		apiHelpers,
		journalEditArticlePage,
		portalLogChecker,
		site,
		webContentDisplayPage,
	}) => {
		const webContentTitle = 'WC WebContent Title';
		const documentTitle = 'DM Document Title';
		const pageName = 'Test Page';
		const structureName = 'WC Structure Name';

		let testPage;
		let document;
		let structure;

		await test.step('Given: User adds a new site with a page', async () => {
			testPage = await apiHelpers.headlessDelivery.createSitePage({
				siteId: site.id,
				title: pageName,
			});
		});

		await test.step('And: Add a new file on this site', async () => {
			document = await apiHelpers.headlessDelivery.postDocument(
				site.id,
				createReadStream(
					path.join(__dirname, '/dependencies/Document.jpg')
				),
				{
					description: 'DM Document Description',
					fileName: 'Document_1.jpg',
					title: documentTitle,
				}
			);
		});

		await test.step('And: Add a new WC structure with 2 fields', async () => {
			const dataDefinition = getDataStructureDefinition({
				defaultLanguageId: 'en_US',
				fields: [
					{dataType: 'image', fieldType: 'image', name: 'image'},
					{
						dataType: 'link-to-page',
						fieldType: 'link_to_layout',
						name: 'linktopage',
					},
				],
				name: structureName,
			});

			structure = await apiHelpers.dataEngine.createStructure(
				site.id,
				dataDefinition
			);
		});

		await test.step('And: Add a new WC based on the structure and publish it', async () => {
			const fileEntry =
				await apiHelpers.jsonWebServicesDocumentLibrary.getFileEntry(
					document.id
				);

			const imageData = {
				fileEntryId: String(fileEntry.fileEntryId),
				groupId: String(fileEntry.groupId),
				name: document.fileName,
				title: document.title,
				type: 'document',
				url: `/documents/d/${site.key}/dm-document-title`,
				uuid: fileEntry.uuid,
			};

			const linkData = {
				groupId: String(site.id),
				id: testPage.uuid,
				layoutId: String(testPage.id),
				name: `Pages > ${pageName}`,
				privateLayout: false,
				returnType:
					'com.liferay.item.selector.criteria.UUIDItemSelectorReturnType',
				title: pageName,
				value: testPage.uuid,
			};

			const contentXML = `<?xml version="1.0"?>
<root available-locales="en_US" default-locale="en_US" version="1.0">
    <dynamic-element field-reference="image" index-type="keyword" instance-id="ivrne27a" name="image" type="image">
        <dynamic-content language-id="en_US"><![CDATA[${JSON.stringify(imageData)}]]></dynamic-content>
    </dynamic-element>
    <dynamic-element field-reference="linktopage" index-type="keyword" instance-id="uz2EEttK" name="linktopage" type="link_to_layout">
        <dynamic-content language-id="en_US"><![CDATA[${JSON.stringify(linkData)}]]></dynamic-content>
    </dynamic-element>
</root>`;

			await apiHelpers.jsonWebServicesJournal.addStructuredWebContent({
				contentXML,
				ddmStructureId: structure.id,
				groupId: site.id,
				titleMap: {en_US: webContentTitle},
			});
		});

		await test.step('When: User deletes the file and the test page, then export the web content', async () => {
			await apiHelpers.headlessDelivery.deleteDocument(document.id);
			await apiHelpers.jsonWebServicesLayout.deleteLayout(testPage.id);

			await webContentDisplayPage.gotoWebContentAdmin(site.name);
			await journalEditArticlePage.selectExportImportOption();
			await journalEditArticlePage.export();
		});

		await test.step('Then: The export of the WC with deleted reference is successful without error on the console', async () => {
			expect(
				await portalLogChecker.isConsoleTextPresent(
					'com.liferay.exportimport.kernel.lar.PortletDataException'
				)
			).toBe(false);
		});
	});
});

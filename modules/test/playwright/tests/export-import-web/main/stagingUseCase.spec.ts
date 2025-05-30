/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';
import {createReadStream, readdirSync} from 'fs';
import path from 'path';

import {dataApiHelpersTest} from '../../../fixtures/dataApiHelpersTest';
import {dataRemoteApiHelpersTest} from '../../../fixtures/dataRemoteApiHelpersTest';
import {featureFlagsTest} from '../../../fixtures/featureFlagsTest';
import {loginTest} from '../../../fixtures/loginTest';
import {pageEditorPagesTest} from '../../../fixtures/pageEditorPagesTest';
import {pageViewModePagesTest} from '../../../fixtures/pageViewModePagesTest';
import {remotePageTest} from '../../../fixtures/remotePageTest';
import {webContentDisplayPageTest} from '../../../fixtures/webContentDisplayPageTest';
import getRandomString from '../../../utils/getRandomString';
import {reloadUntilVisible} from '../../../utils/reloadUntilVisible';
import getBasicWebContentStructureId from '../../../utils/structured-content/getBasicWebContentStructureId';
import {remoteStagingPagesTest} from '../../export-import-service/main/fixtures/remoteStagingPagesTest';
import {journalPagesTest} from '../../journal-web/main/fixtures/journalPagesTest';
import getDataStructureDefinition from '../../journal-web/main/utils/getDataStructureDefinition';
import {exportImportConfig} from './export_import.config';
import {stagingConfigurationPageTest} from './fixtures/stagingConfigurationPageTest';
import {stagingPageTest} from './fixtures/stagingPageTest';
import {unzipAndCheckFolder} from './utils/stagingUtil';
import {waitForAlert} from '../../../utils/waitForAlert';


const remotePort = '9080';
const remotePage = remotePageTest(remotePort);

export const test = mergeTests(
	dataApiHelpersTest,
	dataRemoteApiHelpersTest(remotePage, remotePort),
	featureFlagsTest({
		'LPD-35914': {enabled: true, system: true},
	}),
	loginTest(),
	journalPagesTest,
	pageEditorPagesTest,
	pageViewModePagesTest,
	stagingPageTest,
	stagingConfigurationPageTest,
	remoteStagingPagesTest,
	webContentDisplayPageTest
);

test(
	'can publish web content with URL references to live via remote staging.',
	{tag: '@LPS-159626'},
	async ({
		apiHelpers,
		journalEditTemplatePage,
		journalStructuresPage,
		page,
		pageEditorPage,
		remoteApiHelpers,
		remotePage,
		remoteStagingPage,
		webContentDisplayPage,
		widgetPagePage,
	}) => {
		const site = await apiHelpers.headlessSite.createSite({
			name: 'site-' + getRandomString(),
		});

		apiHelpers.data.push({id: site.id, type: 'site'});

		const layouts: Array<Layout> = [];

		for (const i of [1, 2, 3]) {
			let layout = await apiHelpers.jsonWebServicesLayout.addLayout({
				groupId: site.id,
				title: `Page ${i}`,
			});

			layouts.push(layout);

			for (const j of [1, 2]) {
				layout = await apiHelpers.jsonWebServicesLayout.addLayout({
					groupId: site.id,
					parentLayoutId: layout.layoutId,
					title: `Page ${i}${j}`,
				});

				layouts.push(layout);

				if (i === 1 && j === 1) {
					layout = await apiHelpers.jsonWebServicesLayout.addLayout({
						groupId: site.id,
						parentLayoutId: layout.layoutId,
						title: 'Page 111',
					});
					layouts.push(layout);
				}
			}
		}

		const remoteSite = await remoteApiHelpers.headlessSite.createSite({
			name: getRandomString(),
		});

		await remoteApiHelpers.data.push({id: remoteSite.id, type: 'site'});

		await apiHelpers.jsonWebServicesStaging.enableRemoteStaging({
			groupId: site.id,
			remoteGroupId: remoteSite.id,
			remotePort,
		});

		for (const layout of layouts) {
			await pageEditorPage.goto(layout, site.friendlyUrlPath);

			await widgetPagePage.addPortlet('Web Content Display');
			await widgetPagePage.addPortlet('Web Content Display');
		}
		const fields: Array<any> = [];
		const pageNumbers = [1, 11, 111, 12, 2, 21, 22, 3, 31, 32];

		for (const num of pageNumbers) {
			fields.push({name: `Openpage${num}`, repeatable: false});
			fields.push({name: `URL${num}`, repeatable: false});
		}

		await journalStructuresPage.goto(site.friendlyUrlPath);
		const dataDefinition = getDataStructureDefinition({
			defaultLanguageId: 'en_US',

			fields,
			name: getRandomString(),
		});

		const structure = await apiHelpers.dataEngine.createStructure(
			site.id,
			dataDefinition
		);

		const templateScript =
			'<p> <a href="${URL1.getData()}">${Openpage1.getData()}</a> </p><p> <a href="${URL2.getData()}">${Openpage2.getData()}</a> </p><p> <a href="${URL3.getData()}">${Openpage3.getData()}</a> </p><p> <a href="${URL11.getData()}">${Openpage11.getData()}</a> </p><p> <a href="${URL12.getData()}">${Openpage12.getData()}</a> </p><p> <a href="${URL111.getData()}">${Openpage111.getData()}</a> </p><p> <a href="${URL21.getData()}">${Openpage21.getData()}</a> </p><p> <a href="${URL22.getData()}">${Openpage22.getData()}</a> </p><p> <a href="${URL31.getData()}">${Openpage31.getData()}</a> </p><p> <a href="${URL32.getData()}">${Openpage32.getData()}</a> </p>';

		await journalEditTemplatePage.goto(site.friendlyUrlPath);
		await journalEditTemplatePage.editTemplate(
			getRandomString(),
			templateScript
		);
		await journalEditTemplatePage.saveTemplate();

		const webContentTitle = getRandomString();
		await apiHelpers.jsonWebServicesJournal.addWebContent({
			ddmStructureId: structure.id,
			ddmTemplateKey: null,
			groupId: site.id,
			titleMap: {en_US: webContentTitle},
		});
		await webContentDisplayPage.gotoWebContentAdmin(site.name);
		await page.getByRole('link', {name: webContentTitle}).click();

		let i = 0;
		for (const layout of layouts) {
			await page
				.getByLabel(`Openpage${pageNumbers[i]}`, {exact: true})
				.fill(layout.nameCurrentValue);
			await page
				.getByLabel(`URL${pageNumbers[i]}`, {exact: true})
				.fill(`/web${site.friendlyUrlPath}` + layout.friendlyURL);
			i++;
		}

		await page.getByLabel('Select and Confirm Publish').click();
		await page.getByRole('menuitem', {name: 'Publish'}).click();
	
		await waitForAlert(
			page,
			'Success'
		);
		
	
		const fields2: Array<any> = [];

		fields2.push({name: 'Content1', repeatable: false});
		fields2.push({name: 'Content2', repeatable: false});

		const dataDefinition2 = getDataStructureDefinition({
			defaultLanguageId: 'en_US',

			fields: fields2,
			name: getRandomString(),
		});

		const structure2 = await apiHelpers.dataEngine.createStructure(
			site.id,
			dataDefinition2
		);

		await webContentDisplayPage.gotoWebContentAdmin(site.name);

		for (const num of pageNumbers) {
			await apiHelpers.jsonWebServicesJournal.addWebContent({
				ddmStructureId: structure2.id,
				ddmTemplateKey: null,
				groupId: site.id,
				titleMap: {en_US: `Title-${num}`},
			});

			await reloadUntilVisible({
				myLocator: page
					.getByRole('link', {name: `Title-${num}`}),
				page,
			});

			let resultVisible = false;
			 while (!resultVisible){
				await page.getByRole('link', {name: `Title-${num}`}).click({timeout: 500});
				await page.waitForTimeout(1000);

				resultVisible = await page.getByLabel('Select and Confirm Publish').isVisible();
			}
			

			await reloadUntilVisible({
				myLocator: page
					.getByLabel(`Content2`, {exact: true}),
				page,
			});

			await page
				.getByLabel(`Content1`, {exact: true})
				.fill(`Content-${num}`);
			await page
				.getByLabel(`Content2`, {exact: true})
				.fill(`Text Content-${num}`);

			await page.getByLabel('Select and Confirm Publish').click();
			await page.getByRole('menuitem', {name: 'Publish'}).click();

			await waitForAlert(
				page,
				`Success:Title-${num}`
			);			
		}

		const templateScript2 =
			'<h1>${Title.getData()}</h1><p>${Content.getData()}</p>';

		await journalEditTemplatePage.goto(site.friendlyUrlPath);
		await journalEditTemplatePage.editTemplate(
			getRandomString(),
			templateScript2
		);
		await journalEditTemplatePage.saveTemplate();

		i = 0;
		for (const layout of layouts) {
			await pageEditorPage.goto(layout, site.friendlyUrlPath);
			await webContentDisplayPage.addWebContentWithDisplay({
				pageType: 'content',
				webContentName: webContentTitle,
			});

			await page.waitForTimeout(2000);
			await page.reload();
			await webContentDisplayPage.addWebContentWithDisplay({
				pageType: 'content',
				webContentName: `Title-${pageNumbers[i]}`,
			});
			await page.waitForTimeout(2000);
			i++;
		}

		const remoteUrl = remoteApiHelpers.baseUrl.substring(
			0,
			remoteApiHelpers.baseUrl.length - 3
		);
		for (const layout of layouts) {
			await remoteStagingPage.publishToLive({
				layoutFriendlyURL: layout.friendlyURL,
				siteFriendlyUrl: site.friendlyUrlPath,
			});
			await page.waitForTimeout(500);
		}
		await remotePage.goto(
			`${remoteUrl}/web${remoteSite.friendlyUrlPath}${layouts[0].friendlyURL}`
		);

		for (const num of [111, 21, 3]) {
			const element = await page.getByLabel(`Openpage${num}`).click();

			if (await element.isVisible()) {
				await element.click();
			}

			await expect(
				remotePage.getByRole('heading', {name: `Title-${num}`})
			).toBeVisible();

			expect(page.url()).toContain(`/web/site-name/page-${num}`);
		}
	}
);

test(
	'non modified referred content cannot publish to live when enable include if modified option',
	{tag: '@LPS-167777'},
	async ({apiHelpers, stagingConfigurationPage, stagingPage}) => {
		const site = await apiHelpers.headlessSite.createSite({
			name: 'site-' + getRandomString(),
		});

		apiHelpers.data.push({id: site.id, type: 'site'});

		await apiHelpers.jsonWebServicesLayout.addLayout({
			groupId: site.id,
			title: getRandomString(),
		});

		await stagingPage.goto(site.name);
		await stagingPage.enableLocalStaging();

		const stagingSite =
			await apiHelpers.headlessAdminUser.getSiteByFriendlyUrlPath(
				`${site.friendlyUrlPath}-staging`
			);

		const webContentContent = getRandomString();
		let webContent = await apiHelpers.jsonWebServicesJournal.addWebContent({
			content: webContentContent,
			ddmStructureId: await getBasicWebContentStructureId(apiHelpers),
			groupId: stagingSite.id,
			titleMap: {en_US: getRandomString()},
		});

		const document = await apiHelpers.headlessDelivery.postDocument(
			stagingSite.id,
			createReadStream(
				path.join(__dirname, '/dependencies/Document.jpg')
			),
			{
				fileName: 'Document.jpg',
				title: 'Document.jpg',
			}
		);

		webContent = await apiHelpers.jsonWebServicesJournal.editWebContent(
			{
				content: `<img alt="" data-fileentryid="${document.id}" src="/documents/d${stagingSite.friendlyUrlPath}/Document-jpg">&nbsp;<br>${webContentContent}`,
			},
			stagingSite.id,
			webContent
		);

		await stagingPage.goto(site.name + '-staging');
		await stagingPage.publish();

		await stagingConfigurationPage.goto(site.name);
		await stagingConfigurationPage.disableTemporaryLARdeletion();

		await apiHelpers.jsonWebServicesJournal.editWebContent(
			{title: getRandomString()},
			stagingSite.id,
			webContent
		);

		await stagingPage.goto(site.name + '-staging');
		await stagingPage.publish(['Web Content 1 Items Web']);

		const tomcatDir = exportImportConfig.environment.tomcatDir;

		const files = readdirSync(tomcatDir).filter((file) =>
			file.startsWith('tomcat-')
		);

		const hasFolder = await unzipAndCheckFolder(
			path.resolve(tomcatDir, files[0], 'temp')
		);

		expect(hasFolder).toEqual(false);
	}
);

test('staging publish template with smoke', async ({
	apiHelpers,
	page,
	stagingPage,
	webContentDisplayPage,
	widgetPagePage,
}) => {
	const site = await apiHelpers.headlessSite.createSite({
		name: getRandomString(),
	});

	apiHelpers.data.push({id: site.id, type: 'site'});

	const layout = await apiHelpers.jsonWebServicesLayout.addLayout({
		groupId: site.id,
		options: {type: 'portlet'},
		title: getRandomString(),
	});

	const webContentContent = getRandomString();
	const webContent = await apiHelpers.jsonWebServicesJournal.addWebContent({
		content: webContentContent,
		ddmStructureId: await getBasicWebContentStructureId(apiHelpers),
		groupId: site.id,
		titleMap: {en_US: getRandomString()},
	});

	apiHelpers.data.push({
		id: `${site.id}_${webContent.articleId}`,
		type: 'webContent',
	});

	await stagingPage.goto(site.name);
	await stagingPage.enableLocalStaging();

	const stagingSite =
		await apiHelpers.headlessAdminUser.getSiteByFriendlyUrlPath(
			`${site.friendlyUrlPath}-staging`
		);

	await page.waitForTimeout(2000);
	await widgetPagePage.goto(layout, stagingSite.friendlyUrlPath);
	await page.waitForLoadState('domcontentloaded');

	await widgetPagePage.addPortlet(
		'Web Content Display',
		'Content Management'
	);

	await webContentDisplayPage.addWebContentWithDisplay({
		pageType: 'widget',
		webContentName: webContent.title,
	});

	await page.waitForTimeout(2000);

	await stagingPage.goto(site.name + '-staging');

	const templateName = getRandomString();
	await stagingPage.gotoTemplatePage();
	await stagingPage.addTemplate(templateName);
	await page.reload({waitUntil: 'domcontentloaded'});
	await stagingPage.publishTemplate(templateName);

	await widgetPagePage.goto(layout, site.friendlyUrlPath);

	expect(page.getByText(webContent.title, {exact: true})).toBeVisible();
	expect(page.getByText(webContentContent, {exact: true})).toBeVisible();
});

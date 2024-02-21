/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {exportImportPagesTest} from '../../fixtures/exportImportPages.fixtures';
import {loginTest} from '../../fixtures/loginTest';
import {featureFlagsTest} from '../../fixtures/featureFlagsTest';

import {expect, mergeTests} from '@playwright/test';
import * as path from 'path';
import {open} from 'yauzl'
import { apiHelpersTest } from '../../fixtures/apiHelpersTest';
import { dataMigrationCenterPagesTest } from '../../fixtures/dataMigrationCenterPages';
import { unzipFile } from '../../utils/util';

export const test = mergeTests(
	apiHelpersTest,
	exportImportPagesTest,
	loginTest,
	dataMigrationCenterPagesTest,
	featureFlagsTest({
		'COMMERCE-8087': true,
	}),
);

test('unzip a folder', async ({
	apiHelpers,
	dataMigrationCenterPage,
	page
}) => {
	const objectDefinition = await apiHelpers.objectAdmin.postObjectDefinition({
		active: true,
		externalReferenceCode: `objectDefinition`,
		label: {
			en_US: `stock`,
		},
		name: `Stock`,
		objectFields: [
			{
				DBType: 'String',
				businessType: 'Text',
				externalReferenceCode: 'ObjectFieldERC',
				indexed: true,
				indexedAsKeyword: false,
				indexedLanguageId: 'en_US',
				label: {
					en_US: 'Object Field',
				},
				listTypeDefinitionId: 0,
				name: 'name',
				required: true,
				state: false,
				system: false,
				type: 'String',
			},
		],
		pluralLabel: {
			en_US: `stocks`,
		},
		portlet: true,
		scope: 'company',
		status: {
			code: 0,
		},
	})

	const objectEntry = await apiHelpers.object.postObjectEntry(
		{
			externalReferenceCode: 'stockEntryERC',
			name: "Stock Entry"
		},
		'c/stocks'
	);

	await dataMigrationCenterPage.goto();
	await dataMigrationCenterPage.goToExportFile();

	await dataMigrationCenterPage.exportFile(
		'JSONT',
		'C_Stock (v1_0 - Liferay Object REST)'
	)

	//Then the downloaded zip file contain *.json file.
	// const filePath = path.resolve(__dirname, 'dependencies/Export.zip');

	// await unzipFile(
	// 	filePath,
	// 	require(path.resolve(__dirname, 'dependencies/jsont_objectEntry_import.json')),
	// 	handleUnzipFile
	// );

	await apiHelpers.objectAdmin.deleteObjectDefinition(
		objectDefinition.id
	);
})

function handleUnzipFile(zip, json) {
	return async function (err, readStream) {
		if (err) throw err;
		readStream.on("end", function () {
			zip.readEntry();
		});
		expect(json).toEqual(await _streamToString(readStream));
	};
}

export async function _streamToString(stream) {
    const chunks = [];

    for await (const chunk of stream) {
        chunks.push(Buffer.from(chunk));
    }

    return JSON.parse(Buffer.concat(chunks).toString());
}

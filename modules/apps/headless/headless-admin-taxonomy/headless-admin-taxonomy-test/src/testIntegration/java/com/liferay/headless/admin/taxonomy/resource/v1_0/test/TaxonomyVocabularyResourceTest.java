/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.headless.admin.taxonomy.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.headless.admin.taxonomy.client.dto.v1_0.AssetType;
import com.liferay.headless.admin.taxonomy.client.dto.v1_0.TaxonomyVocabulary;
import com.liferay.headless.admin.taxonomy.client.pagination.Page;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.runner.RunWith;

/**
 * @author Javier Gamarra
 */
@RunWith(Arquillian.class)
public class TaxonomyVocabularyResourceTest
	extends BaseTaxonomyVocabularyResourceTestCase {

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {"assetTypes", "description", "name"};
	}

	@Override
	protected String[] getIgnoredEntityFieldNames() {
		return new String[] {"dateCreated", "dateModified"};
	}

	@Override
	protected TaxonomyVocabulary randomTaxonomyVocabulary() {
		return new TaxonomyVocabulary() {
			{
				assetTypes = new AssetType[] {
					new AssetType() {
						{
							required = RandomTestUtil.randomBoolean();
							subtype = "AllAssetSubtypes";
							type = "AllAssetTypes";
						}
					}
				};
				description = RandomTestUtil.randomString();
				externalReferenceCode = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				name = RandomTestUtil.randomString();
				siteId = testGroup.getGroupId();
			}
		};
	}

	@Override
	protected TaxonomyVocabulary
			testDeleteAssetLibraryTaxonomyVocabularyByExternalReferenceCode_addTaxonomyVocabulary()
		throws Exception {

		return taxonomyVocabularyResource.postAssetLibraryTaxonomyVocabulary(
			testDepotEntry.getDepotEntryId(), randomTaxonomyVocabulary());
	}

	@Override
	protected Long
			testDeleteAssetLibraryTaxonomyVocabularyByExternalReferenceCode_getAssetLibraryId()
		throws Exception {

		return testDepotEntry.getDepotEntryId();
	}

	@Override
	protected TaxonomyVocabulary
			testGetAssetLibraryTaxonomyVocabularyByExternalReferenceCode_addTaxonomyVocabulary()
		throws Exception {

		return testPostAssetLibraryTaxonomyVocabulary_addTaxonomyVocabulary(
			randomTaxonomyVocabulary());
	}

	@Override
	protected Long
			testGetAssetLibraryTaxonomyVocabularyByExternalReferenceCode_getAssetLibraryId()
		throws Exception {

		return testDepotEntry.getDepotEntryId();
	}

	@Override
	protected TaxonomyVocabulary
			testGraphQLGetAssetLibraryTaxonomyVocabularyByExternalReferenceCode_addTaxonomyVocabulary()
		throws Exception {

		return testGetAssetLibraryTaxonomyVocabularyByExternalReferenceCode_addTaxonomyVocabulary();
	}

	@Override
	protected Long
			testGraphQLGetAssetLibraryTaxonomyVocabularyByExternalReferenceCode_getAssetLibraryId()
		throws Exception {

		return testDepotEntry.getDepotEntryId();
	}

	@Override
	protected Long
			testPutAssetLibraryTaxonomyVocabularyByExternalReferenceCode_getAssetLibraryId()
		throws Exception {

		return testDepotEntry.getDepotEntryId();
	}

	private void assertBatchAction(
		Page<TaxonomyVocabulary> page, String action, String method,
		String path) {

		Map<String, Map> actions = page.getActions();

		Map batchAction = actions.get(action);

		Assert.assertNotNull(batchAction);
		Assert.assertEquals(method, batchAction.get("method"));
		assertHrefMatchesPath(
			path,
			batchAction.get(
				"href"
			).toString());
	}

	private void assertHrefMatchesPath(String path, String href) {
		String pathReplaced = path.replaceAll("(\\Q{\\E.*?\\Q}\\E)", "(.*)");

		Pattern p = Pattern.compile(pathReplaced + "/batch");

		Matcher m = p.matcher(href);

		Assert.assertTrue(
			"The " + href + " does not match " + pathReplaced + "/batch",
			m.matches());
	}

}
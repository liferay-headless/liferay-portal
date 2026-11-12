/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.builder.application.publisher.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.headless.builder.application.APIApplication;
import com.liferay.headless.builder.test.BaseTestCase;
import com.liferay.object.test.util.OpenAPIDocumentTestUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.vulcan.application.HeadlessApplicationProvider;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Petteri Karttunen
 */
@FeatureFlag("LPS-178642")
@RunWith(Arquillian.class)
public class APIApplicationPublisherTest extends BaseTestCase {

	@Test
	public void testPublish() throws Exception {
		UserTestUtil.setUser(TestPropsValues.getUser());

		String baseURL = StringUtil.toLowerCase(RandomTestUtil.randomString());
		String externalReferenceCode = RandomTestUtil.randomString();

		assertSuccessfulJSONObject(
			JSONUtil.put(
				"applicationStatus", "published"
			).put(
				"baseURL", baseURL
			).put(
				"externalReferenceCode", externalReferenceCode
			).put(
				"title", RandomTestUtil.randomString()
			).toString(),
			"headless-builder/applications", Http.Method.POST);

		Assert.assertNotNull(
			OpenAPIDocumentTestUtil.getContent(
				"/c/" + baseURL, _headlessApplicationProvider));

		String apiSchemaExternalReferenceCode = RandomTestUtil.randomString();

		assertSuccessfulJSONObject(
			JSONUtil.put(
				"externalReferenceCode", apiSchemaExternalReferenceCode
			).put(
				"mainObjectDefinitionERC", "L_API_APPLICATION"
			).put(
				"name", RandomTestUtil.randomString()
			).put(
				"r_apiApplicationToAPISchemas_l_apiApplicationERC",
				externalReferenceCode
			).toString(),
			"headless-builder/schemas", Http.Method.POST);

		String path =
			StringPool.FORWARD_SLASH +
				StringUtil.toLowerCase(RandomTestUtil.randomString());

		assertSuccessfulJSONObject(
			JSONUtil.put(
				"httpMethod", "get"
			).put(
				"name", RandomTestUtil.randomString()
			).put(
				"path", path
			).put(
				"r_apiApplicationToAPIEndpoints_l_apiApplicationERC",
				externalReferenceCode
			).put(
				"r_responseAPISchemaToAPIEndpoints_l_apiSchemaERC",
				apiSchemaExternalReferenceCode
			).put(
				"retrieveType",
				APIApplication.Endpoint.RetrieveType.COLLECTION.getValue()
			).put(
				"scope", APIApplication.Endpoint.Scope.COMPANY.getValue()
			).toString(),
			"headless-builder/endpoints", Http.Method.POST);

		String content = OpenAPIDocumentTestUtil.getContent(
			"/c/" + baseURL, _headlessApplicationProvider);

		Assert.assertTrue(content.contains(path));
	}

	@Inject
	private HeadlessApplicationProvider _headlessApplicationProvider;

}
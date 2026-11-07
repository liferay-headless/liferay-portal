/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.vulcan.internal.cache.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.vulcan.application.OpenAPIDocumentCacheInvalidator;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Petteri Karttunen
 */
@RunWith(Arquillian.class)
public class OpenAPIDocumentCacheInvalidatorTest
	extends BaseOpenAPIDocumentCacheTestCase {

	@Test
	public void testInvalidate() throws Exception {
		String serverURL = RandomTestUtil.randomString();

		Assert.assertEquals(
			"{\"openapi\": \"1\"}", getContentString(serverURL));

		_openAPIDocumentCacheInvalidator.invalidate(
			RandomTestUtil.randomLong(), basePath);
		_openAPIDocumentCacheInvalidator.invalidate(
			TestPropsValues.getCompanyId(),
			"/" + RandomTestUtil.randomString());

		Assert.assertEquals(
			"{\"openapi\": \"1\"}", getContentString(serverURL));

		_openAPIDocumentCacheInvalidator.invalidate(
			TestPropsValues.getCompanyId(),
			basePath + "/" + RandomTestUtil.randomString());

		Assert.assertEquals(
			"{\"openapi\": \"2\"}", getContentString(serverURL));

		_openAPIDocumentCacheInvalidator.invalidate(
			TestPropsValues.getCompanyId());

		Assert.assertEquals(
			"{\"openapi\": \"3\"}", getContentString(serverURL));
	}

	@Inject
	private OpenAPIDocumentCacheInvalidator _openAPIDocumentCacheInvalidator;

}
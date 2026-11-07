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
public class OpenAPIDocumentCacheTest extends BaseOpenAPIDocumentCacheTestCase {

	@Test
	public void testGetContentWhenInvalidatedDuringGeneration()
		throws Exception {

		long companyId = TestPropsValues.getCompanyId();
		String serverURL = RandomTestUtil.randomString();

		testResource.setRunnable(
			() -> _openAPIDocumentCacheInvalidator.invalidate(companyId));

		Assert.assertEquals(
			"{\"openapi\": \"1\"}", getContentString(serverURL));

		testResource.setRunnable(null);

		Assert.assertEquals(
			"{\"openapi\": \"2\"}", getContentString(serverURL));
	}

	@Inject
	private OpenAPIDocumentCacheInvalidator _openAPIDocumentCacheInvalidator;

}
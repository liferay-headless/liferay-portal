/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.vulcan.internal.feature.flag.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.test.util.FeatureFlagTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.vulcan.internal.cache.test.BaseOpenAPIDocumentCacheTestCase;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Petteri Karttunen
 */
@RunWith(Arquillian.class)
public class OpenAPIDocumentCacheFeatureFlagListenerTest
	extends BaseOpenAPIDocumentCacheTestCase {

	@Test
	public void testOnValue() throws Exception {
		String serverURL = RandomTestUtil.randomString();

		Assert.assertEquals(
			"{\"openapi\": \"1\"}", getContentString(serverURL));

		FeatureFlagTestUtil.invokeFeatureFlagListeners(
			RandomTestUtil.randomLong(), RandomTestUtil.randomBoolean(),
			RandomTestUtil.randomString());

		Assert.assertEquals(
			"{\"openapi\": \"1\"}", getContentString(serverURL));

		FeatureFlagTestUtil.invokeFeatureFlagListeners(
			TestPropsValues.getCompanyId(), RandomTestUtil.randomBoolean(),
			RandomTestUtil.randomString());

		Assert.assertEquals(
			"{\"openapi\": \"2\"}", getContentString(serverURL));

		FeatureFlagTestUtil.invokeFeatureFlagListeners(
			CompanyConstants.SYSTEM, RandomTestUtil.randomBoolean(),
			RandomTestUtil.randomString());

		Assert.assertEquals(
			"{\"openapi\": \"3\"}", getContentString(serverURL));
	}

}
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.util;

import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Petteri Karttunen
 */
public class OpenAPIBriefUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		_companyId = RandomTestUtil.randomLong();

		_openAPIBriefs = ReflectionTestUtil.getFieldValue(
			OpenAPIBriefUtil.class, "_openAPIBriefs");

		_openAPIBriefs.clear();
	}

	@Test
	public void testGetToolSetNameWhenTheBriefsAreNotCached() throws Exception {
		Assert.assertEquals(
			"c-npeprobes",
			OpenAPIBriefUtil.getToolSetName(_companyId, "/c/npeprobes"));
	}

	@Test
	public void testGetToolSetNameWhenTheBriefsPredateTheApplicationWasPublished()
		throws Exception {

		_setOpenAPIBriefs("c-other", "/c/other");

		Assert.assertEquals(
			"c-npeprobes",
			OpenAPIBriefUtil.getToolSetName(_companyId, "/c/npeprobes"));
	}

	@Test
	public void testGetToolSetNameWhenTheLongestBasePathWins()
		throws Exception {

		_setOpenAPIBriefs("o-headless-delivery", "/o/headless-delivery");
		_setOpenAPIBriefs(
			"o-headless-delivery-v1.0", "/o/headless-delivery/v1.0");

		Assert.assertEquals(
			"o-headless-delivery-v1.0",
			OpenAPIBriefUtil.getToolSetName(
				_companyId, "/o/headless-delivery/v1.0/blog-postings"));
	}

	@Test
	public void testGetToolSetNameWhenThePathIsBlank() throws Exception {
		Assert.assertNull(OpenAPIBriefUtil.getToolSetName(_companyId, null));
		Assert.assertNull(OpenAPIBriefUtil.getToolSetName(_companyId, ""));
	}

	@Test
	public void testGetToolSetNameWhenThePathIsNotAnObjectPath()
		throws Exception {

		Assert.assertNull(
			OpenAPIBriefUtil.getToolSetName(
				_companyId, "/o/headless-delivery/v1.0/blog-postings"));
	}

	private void _setOpenAPIBriefs(String toolSetName, String basePath) {
		Map<String, OpenAPIBrief> openAPIBriefs =
			_openAPIBriefs.computeIfAbsent(_companyId, key -> new HashMap<>());

		openAPIBriefs.put(
			toolSetName,
			new OpenAPIBrief(
				basePath, RandomTestUtil.randomString(), "/openapi.json"));
	}

	private long _companyId;
	private Map<Long, Map<String, OpenAPIBrief>> _openAPIBriefs;

}
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.search.index.util;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Petteri Karttunen
 */
public class ExpansionUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGetExpansions() {
		Assert.assertArrayEquals(
			new String[] {
				"create blog posting in a site", "add blog posting in a site",
				"make blog posting in a site", "write blog posting in a site"
			},
			ExpansionUtil.getExpansions(
				null, false, "blog posting", "post",
				"/sites/{siteId}/blog-postings", "postSiteBlogPosting"));
	}

	@Test
	public void testGetExpansionsWithABatchTool() {
		String[] expansions = ExpansionUtil.getExpansions(
			null, true, "blog posting", "post",
			"/sites/{siteId}/blog-postings/batch", "postSiteBlogPostingBatch");

		Assert.assertEquals(
			"batch create blog postings in a site", expansions[0]);

		for (String expansion : expansions) {
			Assert.assertTrue(
				expansion, StringUtil.startsWith(expansion, "batch "));
		}
	}

	@Test
	public void testGetExpansionsWithACollectionTool() {
		Assert.assertArrayEquals(
			new String[] {
				"list blog postings in a site", "show blog postings in a site",
				"browse blog postings in a site",
				"find blog postings in a site",
				"get all blog postings in a site", "see blog postings in a site"
			},
			ExpansionUtil.getExpansions(
				null, false, "blog posting", "get",
				"/sites/{siteId}/blog-postings", "getSiteBlogPostingsPage"));
	}

	@Test
	public void testGetExpansionsWithAPathParameterInTheToolName() {
		String[] expansions = ExpansionUtil.getExpansions(
			null, false, "blog posting", "get",
			"/sites/{siteId}/blog-postings/by-external-reference-code" +
				"/{externalReferenceCode}",
			"getSiteBlogPostingByExternalReferenceCode");

		Assert.assertEquals(
			"get blog posting in a site by external reference code",
			expansions[0]);
	}

	@Test
	public void testGetExpansionsWithAnActionMarker() {
		String[] expansions = ExpansionUtil.getExpansions(
			"Subscribe", false, "blog posting", "put",
			"/sites/{siteId}/blog-postings/subscribe",
			"putSiteBlogPostingSubscribe");

		Assert.assertEquals(Arrays.toString(expansions), 3, expansions.length);

		String[] verbs = {"subscribe", "follow", "watch"};

		for (int i = 0; i < expansions.length; i++) {
			Assert.assertTrue(
				expansions[i],
				StringUtil.startsWith(expansions[i], verbs[i] + " "));
			Assert.assertTrue(
				expansions[i], expansions[i].contains("blog posting"));
		}
	}

	@Test
	public void testGetExpansionsWithTheEntityAsTheScope() {
		String[] expansions = ExpansionUtil.getExpansions(
			null, false, "site", "get",
			"/sites/by-external-reference-code/{externalReferenceCode}",
			"getSiteByExternalReferenceCode");

		Assert.assertEquals(
			"get site by external reference code", expansions[0]);

		for (String expansion : expansions) {
			Assert.assertFalse(expansion, expansion.contains("in a site"));
		}
	}

	@Test
	public void testGetExpansionsWithoutAnEntity() {
		Assert.assertEquals(
			0,
			ExpansionUtil.getExpansions(
				null, false, StringPool.BLANK, "post", "/sites", "postSite").
					length);
		Assert.assertEquals(
			0,
			ExpansionUtil.getExpansions(
				null, false, "site", "head", "/sites", "headSite").length);
	}

}
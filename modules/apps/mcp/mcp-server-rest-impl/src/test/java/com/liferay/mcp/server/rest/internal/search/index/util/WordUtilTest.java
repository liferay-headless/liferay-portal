/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.search.index.util;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Petteri Karttunen
 */
public class WordUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testIsPlural() {
		Assert.assertTrue(WordUtil.isPlural("sites"));
		Assert.assertTrue(WordUtil.isPlural("blog-postings"));
		Assert.assertFalse(WordUtil.isPlural("site"));
		Assert.assertFalse(WordUtil.isPlural("address"));
	}

	@Test
	public void testIsPluralWhenTheSingularEndsInS() {
		Assert.assertFalse(WordUtil.isPlural("status"));
		Assert.assertFalse(WordUtil.isPlural("by-status"));
		Assert.assertFalse(WordUtil.isPlural("asset-metrics"));
		Assert.assertTrue(WordUtil.isPlural("navigation-menus"));
	}

	@Test
	public void testNormalize() {
		Assert.assertEquals(
			"blogpostings", WordUtil.normalize("Blog-Postings"));
		Assert.assertEquals("blogpostings", WordUtil.normalize("BlogPostings"));
	}

	@Test
	public void testToPlural() {
		Assert.assertEquals("addresses", WordUtil.toPlural("address"));
		Assert.assertEquals("boxes", WordUtil.toPlural("box"));
		Assert.assertEquals("categories", WordUtil.toPlural("category"));
		Assert.assertEquals("keys", WordUtil.toPlural("key"));
		Assert.assertEquals("sites", WordUtil.toPlural("site"));
		Assert.assertEquals("sites", WordUtil.toPlural("sites"));
		Assert.assertEquals("status", WordUtil.toPlural("status"));
		Assert.assertEquals(StringPool.BLANK, WordUtil.toPlural(null));
	}

	@Test
	public void testToSingular() {
		Assert.assertEquals("address", WordUtil.toSingular("addresses"));
		Assert.assertEquals("box", WordUtil.toSingular("boxes"));
		Assert.assertEquals("category", WordUtil.toSingular("categories"));
		Assert.assertEquals("site", WordUtil.toSingular("site"));
		Assert.assertEquals("site", WordUtil.toSingular("sites"));
		Assert.assertEquals("status", WordUtil.toSingular("status"));
	}

	@Test
	public void testToWords() {
		Assert.assertEquals("blog posting", WordUtil.toWords("BlogPosting"));
		Assert.assertEquals(
			"get site blog postings page",
			WordUtil.toWords("getSiteBlogPostingsPage"));
		Assert.assertEquals(
			"open api document", WordUtil.toWords("OpenAPIDocument"));
		Assert.assertEquals(StringPool.BLANK, WordUtil.toWords(null));
	}

}
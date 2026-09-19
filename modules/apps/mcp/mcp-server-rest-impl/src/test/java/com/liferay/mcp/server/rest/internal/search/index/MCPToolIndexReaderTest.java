/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.search.index;

import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Petteri Karttunen
 */
public class MCPToolIndexReaderTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGetEntityNameWord() {
		Assert.assertEquals(
			"posting", _getEntityNameWord("create a blog posting in a site"));
	}

	@Test
	public void testGetEntityNameWordWhenABoundaryWordFollowsTheOfPhrase() {
		Assert.assertEquals(
			"site", _getEntityNameWord("list entries of a site in a folder"));
	}

	@Test
	public void testGetEntityNameWordWhenTheOfPhraseEndsWithAnArticle() {
		Assert.assertEquals(
			"postings", _getEntityNameWord("list the blog postings of the"));
	}

	@Test
	public void testGetEntityNameWordWhenTheOfPhraseNamesTheEntity() {
		Assert.assertEquals(
			"site", _getEntityNameWord("list the blog entries of a site"));
	}

	@Test
	public void testGetEntityNameWordWhenTheSearchHasAssociationWords() {
		Assert.assertNull(_getEntityNameWord("assign a role to a user"));
	}

	@Test
	public void testHasBatchWords() {
		Assert.assertTrue(_hasBatchWords("delete twenty users at once"));
		Assert.assertTrue(_hasBatchWords("delete 20 users"));
	}

	@Test
	public void testHasBatchWordsWhenTheNumberIsAnIdentifier() {
		Assert.assertFalse(_hasBatchWords("update blog posting 40567"));
		Assert.assertFalse(_hasBatchWords("get site 20123 details"));
	}

	private String _getEntityNameWord(String search) {
		return ReflectionTestUtil.invoke(
			_mcpToolIndexReader, "_getEntityNameWord",
			new Class<?>[] {String.class}, search);
	}

	private boolean _hasBatchWords(String search) {
		return ReflectionTestUtil.invoke(
			_mcpToolIndexReader, "_hasBatchWords",
			new Class<?>[] {String.class}, search);
	}

	private final MCPToolIndexReader _mcpToolIndexReader =
		new MCPToolIndexReader();

}
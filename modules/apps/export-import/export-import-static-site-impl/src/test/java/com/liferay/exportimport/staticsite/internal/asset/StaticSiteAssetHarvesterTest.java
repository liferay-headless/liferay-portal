/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.staticsite.internal.asset;

import com.liferay.exportimport.staticsite.internal.util.StaticSitePathUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * Exercises the rewriter against the shapes the classic theme actually emits.
 *
 * @author Alejandro Tardín
 */
public class StaticSiteAssetHarvesterTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_document = Jsoup.parse(_HTML);

		_staticSiteAssetHarvester = new StaticSiteAssetHarvester(
			"http://localhost:8080", "/web/guest",
			HashMapBuilder.put(
				"/about", "about/index.html"
			).put(
				"/about/team", "about/team/index.html"
			).put(
				"/home", StaticSitePathUtil.INDEX_FILE_NAME
			).build());

		_staticSiteAssetHarvester.rewrite(_document, "about/index.html");
	}

	@Test
	public void testDropsPortalServedReferences() {
		Assert.assertEquals("#", _getAttribute("form", "action"));

		Assert.assertTrue(
			_staticSiteAssetHarvester.getDroppedURLs(
			).toString(),
			_staticSiteAssetHarvester.getDroppedURLs(
			).contains(
				"/c/portal/login"
			));
	}

	@Test
	public void testLeavesAnchorsAndExternalURLsAlone() {
		Assert.assertEquals("#main-content", _getAttribute("#skip", "href"));
		Assert.assertEquals(
			"https://liferay.com", _getAttribute("#external", "href"));
	}

	@Test
	public void testRecordsEveryAssetItRewrote() {
		Map<String, String> assetPaths =
			_staticSiteAssetHarvester.getAssetPaths();

		Assert.assertTrue(
			assetPaths.toString(),
			assetPaths.containsValue("_assets/o/classic-theme/css/main.css"));
		Assert.assertTrue(
			assetPaths.toString(),
			assetPaths.containsValue("_assets/o/frontend-js-web/main.js"));
		Assert.assertTrue(
			assetPaths.toString(),
			assetPaths.containsValue(
				"_assets/o/classic-theme/images/hero.jpg"));
	}

	@Test
	public void testRewritesAPortalQualifiedPageLinkToItsFile() {
		Assert.assertEquals(
			"../about/index.html", _getAttribute("#absolute", "href"));
	}

	@Test
	public void testRewritesCSSURLsInStyleAttributes() {
		Assert.assertEquals(
			"background-image:url('../_assets/o/classic-theme/images" +
				"/hero.jpg')",
			_getAttribute("#hero", "style"));
	}

	@Test
	public void testRewritesNestedPageLinksRelativeToTheCurrentPage() {
		Assert.assertEquals(
			"../about/team/index.html", _getAttribute("#team", "href"));
	}

	@Test
	public void testRewritesSrcSetPreservingDescriptors() {
		String srcSet = _getAttribute("#logo", "srcset");

		Assert.assertTrue(srcSet, srcSet.contains(" 1x"));
		Assert.assertTrue(srcSet, srcSet.contains(" 2x"));
		Assert.assertFalse(srcSet, srcSet.contains("imageThumbnail"));
		Assert.assertTrue(srcSet, srcSet.startsWith("../_assets/documents/"));
	}

	@Test
	public void testRewritesThemeCSSDroppingCacheBusters() {
		Assert.assertEquals(
			"../_assets/o/classic-theme/css/main.css",
			_getAttribute("#liferayThemeCSS", "href"));
		Assert.assertEquals(
			"../_assets/o/classic-theme/css/clay.css",
			_getAttribute("#liferayAUICSS", "href"));
	}

	@Test
	public void testRewritesThePortalRootToTheDocumentRoot() {
		Assert.assertEquals(
			"../index.html", _getAttribute("#siteRoot", "href"));
	}

	@Test
	public void testRewritesTheRootPageLinkToTheDocumentRoot() {
		Assert.assertEquals("../index.html", _getAttribute("#home", "href"));
	}

	@Test
	public void testRewritesToADifferentSiteAsUnreachable() {
		Assert.assertEquals("#", _getAttribute("#other", "href"));
	}

	private String _getAttribute(String cssQuery, String attributeName) {
		return _document.selectFirst(
			cssQuery
		).attr(
			attributeName
		);
	}

	private static final String _HTML = String.join(
		"", "<!DOCTYPE html><html lang=\"en-US\"><head>",
		"<title>About</title>",
		"<link href=\"/o/classic-theme/images/favicon.ico\" rel=\"icon\"/>",
		"<link href=\"/o/classic-theme/css/clay.css?browserId=other",
		"&amp;themeId=classic&amp;colorSchemeId=01&amp;languageId=en_US",
		"&amp;b=7400&amp;t=1756200000000\" id=\"liferayAUICSS\"",
		" rel=\"stylesheet\"/>",
		"<link href=\"/o/classic-theme/css/main.css?browserId=other",
		"&amp;t=1756200000000\" id=\"liferayThemeCSS\" rel=\"stylesheet\"/>",
		"<script src=\"/o/frontend-js-web/main.js?t=1756200000000\">",
		"</script></head><body>",
		"<a href=\"#main-content\" id=\"skip\">Skip</a><nav>",
		"<a href=\"/web/guest/home\" id=\"home\">Home</a>",
		"<a href=\"/web/guest/about\" id=\"about\">About</a>",
		"<a href=\"/web/guest/about/team\" id=\"team\">Team</a>",
		"<a href=\"/web/other-site/x\" id=\"other\">Other</a>",
		"<a href=\"http://localhost:8080/web/guest/about\" ",
		"id=\"absolute\">Absolute</a>",
		"<a href=\"http://localhost:8080\" id=\"siteRoot\">Root</a></nav>",
		"<img id=\"logo\" src=\"/documents/20122/0/logo.png?t=17\"",
		" srcset=\"/documents/20122/0/logo.png?imageThumbnail=1 1x,",
		" /documents/20122/0/logo.png?imageThumbnail=2 2x\"/>",
		"<div id=\"hero\" style=\"background-image:url(",
		"'/o/classic-theme/images/hero.jpg')\"></div>",
		"<form action=\"/c/portal/login\" method=\"post\"></form>",
		"<a href=\"https://liferay.com\" id=\"external\">External</a>",
		"</body></html>");

	private Document _document;
	private StaticSiteAssetHarvester _staticSiteAssetHarvester;

}
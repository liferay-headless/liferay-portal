/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.staticsite.internal.util;

import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Alejandro Tardín
 */
public class StaticSitePathUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGetAssetPathDropsCacheBustingParameters() {
		Assert.assertEquals(
			"_assets/o/classic-theme/css/main.css",
			StaticSitePathUtil.getAssetPath(
				"/o/classic-theme/css/main.css?browserId=other&themeId=" +
					"classic&colorSchemeId=01&languageId=en_US&b=7400&t=1"));
	}

	@Test
	public void testGetAssetPathKeepsMeaningfulParameters() {
		String path = StaticSitePathUtil.getAssetPath(
			"/documents/20122/0/logo.png?imageThumbnail=1&t=17");

		Assert.assertTrue(
			path, path.startsWith("_assets/documents/20122/0/logo-"));
		Assert.assertTrue(path, path.endsWith(".png"));
	}

	@Test
	public void testGetAssetPathSeparatesResourcesDifferingOnlyInQuery() {
		Assert.assertNotEquals(
			StaticSitePathUtil.getAssetPath(
				"/documents/20122/0/logo.png?imageThumbnail=1"),
			StaticSitePathUtil.getAssetPath(
				"/documents/20122/0/logo.png?imageThumbnail=2"));
	}

	@Test
	public void testGetAssetPathStripsHost() {
		Assert.assertEquals(
			"_assets/o/frontend-js-web/main.js",
			StaticSitePathUtil.getAssetPath(
				"http://localhost:8080/o/frontend-js-web/main.js"));
		Assert.assertEquals(
			"_assets/o/frontend-js-web/main.js",
			StaticSitePathUtil.getAssetPath(
				"//cdn.example.com/o/frontend-js-web/main.js"));
	}

	@Test
	public void testGetDepth() {
		Assert.assertEquals(0, StaticSitePathUtil.getDepth("index.html"));
		Assert.assertEquals(1, StaticSitePathUtil.getDepth("about/index.html"));
		Assert.assertEquals(
			2, StaticSitePathUtil.getDepth("about/team/index.html"));
	}

	@Test
	public void testGetPagePathNests() {
		Assert.assertEquals(
			"about/team/index.html",
			StaticSitePathUtil.getPagePath("/about/team"));
	}

	@Test
	public void testGetPagePathOfHomeIsTheRootIndex() {
		Assert.assertEquals("index.html", StaticSitePathUtil.getPagePath("/"));
		Assert.assertEquals("index.html", StaticSitePathUtil.getPagePath(""));
	}

	@Test
	public void testGetRelativeReferenceResolvesToTheSameFileFromAnyDepth() {

		// A page at any depth must reach the same harvested asset

		String assetPath = StaticSitePathUtil.getAssetPath(
			"/o/classic-theme/css/main.css");

		Assert.assertEquals(
			"_assets/o/classic-theme/css/main.css",
			StaticSitePathUtil.getRelativeReference(assetPath, 0));
		Assert.assertEquals(
			"../../_assets/o/classic-theme/css/main.css",
			StaticSitePathUtil.getRelativeReference(
				assetPath,
				StaticSitePathUtil.getDepth("about/team/index.html")));
	}

	@Test
	public void testIsHarvestableAcceptsStaticPortalPaths() {
		Assert.assertTrue(
			StaticSitePathUtil.isHarvestable("/o/classic-theme/css/main.css"));
		Assert.assertTrue(
			StaticSitePathUtil.isHarvestable("/image/company_logo?img_id=1"));
		Assert.assertTrue(
			StaticSitePathUtil.isHarvestable("/documents/20122/0/logo.png"));
		Assert.assertTrue(
			StaticSitePathUtil.isHarvestable(
				"http://localhost:8080/o/frontend-js-web/main.js"));
	}

	@Test
	public void testIsHarvestableRejectsPortalServedPaths() {
		Assert.assertFalse(StaticSitePathUtil.isHarvestable("/c/portal/login"));
		Assert.assertFalse(
			StaticSitePathUtil.isHarvestable(
				"/o/headless-delivery/v1.0/pages"));
		Assert.assertFalse(StaticSitePathUtil.isHarvestable("#main-content"));
		Assert.assertFalse(
			StaticSitePathUtil.isHarvestable("mailto:someone@example.com"));
		Assert.assertFalse(
			StaticSitePathUtil.isHarvestable("data:image/gif;base64,R0lG"));
	}

	@Test
	public void testStripPortalURLLeavesForeignHostsAlone() {
		Assert.assertEquals(
			"https://example.com/o/x",
			StaticSitePathUtil.stripPortalURL(
				"https://example.com/o/x", "http://localhost:8080"));
	}

	@Test
	public void testStripPortalURLReducesTheSiteRootToASlash() {
		Assert.assertEquals(
			"/",
			StaticSitePathUtil.stripPortalURL(
				"http://localhost:8080", "http://localhost:8080"));
	}

	@Test
	public void testStripPortalURLReducesToAPath() {
		Assert.assertEquals(
			"/web/guest/about",
			StaticSitePathUtil.stripPortalURL(
				"http://localhost:8080/web/guest/about",
				"http://localhost:8080"));
	}

}
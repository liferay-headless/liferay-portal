/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.staticsite.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.exportimport.kernel.configuration.ExportImportConfigurationSettingsMapFactoryUtil;
import com.liferay.exportimport.kernel.configuration.constants.ExportImportConfigurationConstants;
import com.liferay.exportimport.kernel.model.ExportImportConfiguration;
import com.liferay.exportimport.kernel.service.ExportImportConfigurationLocalServiceUtil;
import com.liferay.exportimport.kernel.service.ExportImportLocalServiceUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.service.LayoutLocalServiceUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Drives an export process end to end with the static HTML output format and
 * asserts on the archive it produces.
 *
 * @author Alejandro Tardín
 */
@RunWith(Arquillian.class)
public class StaticSiteExportTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_homeLayout = _addLayout(
			"home", LayoutConstants.DEFAULT_PARENT_LAYOUT_ID);

		_aboutLayout = _addLayout(
			"about", LayoutConstants.DEFAULT_PARENT_LAYOUT_ID);

		_teamLayout = _addLayout("team", _aboutLayout.getLayoutId());
	}

	@Test
	public void testExportHarvestsTheAssetsThePagesReference()
		throws Exception {

		Map<String, String> entries = _export();

		long assetCount = entries.keySet(
		).stream(
		).filter(
			name -> name.startsWith("_assets/")
		).count();

		Assert.assertTrue(
			"Expected harvested assets, found none in " + entries.keySet(),
			assetCount > 0);
	}

	@Test
	public void testExportLeavesTheModuleLoaderPointingAtThePortal()
		throws Exception {

		// Known gap. The JS module loader publishes its path map inside inline
		// script, where a DOM rewriter cannot reach it, so the loader still
		// resolves modules against the portal. This test pins the size of the
		// problem rather than asserting it away

		Map<String, String> entries = _export();

		String nested = entries.get("about/team/index.html");

		Matcher matcher = _absolutePathPattern.matcher(nested);

		int count = 0;

		while (matcher.find()) {
			count++;
		}

		Assert.assertTrue(
			"Expected the module loader to still carry absolute paths",
			count > 0);

		System.out.println(
			"Absolute portal references left in inline script: " + count);
	}

	@Test
	public void testExportProducesAnApacheReadyTree() throws Exception {
		Map<String, String> entries = _export();

		Assert.assertTrue(
			entries.keySet(
			).toString(),
			entries.containsKey("index.html"));
		Assert.assertTrue(
			entries.keySet(
			).toString(),
			entries.containsKey("home/index.html"));
		Assert.assertTrue(
			entries.keySet(
			).toString(),
			entries.containsKey("about/index.html"));
		Assert.assertTrue(
			entries.keySet(
			).toString(),
			entries.containsKey("about/team/index.html"));

		for (String name : entries.keySet()) {
			Assert.assertFalse(
				"Zip entries must not be absolute: " + name,
				name.startsWith("/"));
		}
	}

	@Test
	public void testExportReportsWhatItDropped() throws Exception {
		Map<String, String> entries = _export();

		Assert.assertTrue(
			entries.keySet(
			).toString(),
			entries.containsKey("MANIFEST.txt"));

		String manifest = entries.get("MANIFEST.txt");

		Assert.assertTrue(manifest, manifest.contains("Pages (3)"));
	}

	@Test
	public void testExportRewritesReferencesRelativeToPageDepth()
		throws Exception {

		Map<String, String> entries = _export();

		String nested = entries.get("about/team/index.html");

		Assert.assertTrue(
			"A nested page must reach the assets directory by walking up",
			nested.contains("../../_assets/"));

		// Every reference the rewriter can see is an attribute, and none of
		// those may survive as an absolute portal path

		Matcher matcher = _absoluteAttributePattern.matcher(nested);

		Assert.assertFalse(matcher.find());

		String root = entries.get("index.html");

		Assert.assertTrue(
			"The root page must reference the assets directory directly",
			root.contains("\"_assets/") || root.contains("'_assets/"));
	}

	private Layout _addLayout(String friendlyURL, long parentLayoutId)
		throws Exception {

		return LayoutLocalServiceUtil.addLayout(
			null, TestPropsValues.getUserId(), _group.getGroupId(), false,
			parentLayoutId, RandomTestUtil.randomString(), StringPool.BLANK,
			StringPool.BLANK, LayoutConstants.TYPE_PORTLET, false,
			"/" + friendlyURL,
			ServiceContextTestUtil.getServiceContext(
				_group.getGroupId(), TestPropsValues.getUserId()));
	}

	private Map<String, String> _export() throws Exception {
		Map<String, Serializable> settingsMap =
			ExportImportConfigurationSettingsMapFactoryUtil.
				buildExportLayoutSettingsMap(
					TestPropsValues.getUserId(), _group.getGroupId(), false,
					new long[] {
						_homeLayout.getLayoutId(), _aboutLayout.getLayoutId(),
						_teamLayout.getLayoutId()
					},
					HashMapBuilder.put(
						"outputFormat", new String[] {"STATIC_HTML"}
					).build(),
					TestPropsValues.getUser(
					).getLocale(),
					TestPropsValues.getUser(
					).getTimeZone());

		ExportImportConfiguration exportImportConfiguration =
			ExportImportConfigurationLocalServiceUtil.
				addExportImportConfiguration(
					TestPropsValues.getUserId(), _group.getGroupId(),
					RandomTestUtil.randomString(),
					RandomTestUtil.randomString(),
					ExportImportConfigurationConstants.TYPE_EXPORT_LAYOUT,
					settingsMap,
					ServiceContextTestUtil.getServiceContext(
						_group.getGroupId(), TestPropsValues.getUserId()));

		File file = ExportImportLocalServiceUtil.exportLayoutsAsFile(
			exportImportConfiguration);

		// Keep a copy of the real archive so it can be unzipped and served

		Files.copy(
			file.toPath(),
			Paths.get(
				System.getProperty("java.io.tmpdir"), "static-site-export.zip"),
			StandardCopyOption.REPLACE_EXISTING);

		return _readZip(file);
	}

	private Map<String, String> _readZip(File file) throws Exception {
		Map<String, String> entries = new LinkedHashMap<>();

		try (ZipFile zipFile = new ZipFile(file)) {
			Enumeration<? extends ZipEntry> enumeration = zipFile.entries();

			while (enumeration.hasMoreElements()) {
				ZipEntry zipEntry = enumeration.nextElement();

				if (zipEntry.isDirectory()) {
					continue;
				}

				try (InputStream inputStream = zipFile.getInputStream(
						zipEntry)) {

					entries.put(
						zipEntry.getName(),
						new String(inputStream.readAllBytes(), "UTF-8"));
				}
			}
		}

		return entries;
	}

	private static final Pattern _absoluteAttributePattern = Pattern.compile(
		"(?:src|href|action)=\"/(?:o|c|documents|image)/[^\"]*\"");
	private static final Pattern _absolutePathPattern = Pattern.compile(
		"/o/[A-Za-z0-9_.@$-]+/");

	private Layout _aboutLayout;

	@DeleteAfterTestRun
	private Group _group;

	private Layout _homeLayout;
	private Layout _teamLayout;

}
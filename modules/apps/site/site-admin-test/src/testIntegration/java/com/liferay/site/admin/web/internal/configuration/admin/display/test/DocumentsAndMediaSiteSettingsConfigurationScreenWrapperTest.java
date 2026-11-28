/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.admin.web.internal.configuration.admin.display.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.configuration.admin.display.ConfigurationScreen;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapDictionary;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.util.Dictionary;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Jürgen Kappler
 */
@RunWith(Arquillian.class)
public class DocumentsAndMediaSiteSettingsConfigurationScreenWrapperTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Test
	public void testExportImportProperties() throws Exception {
		Group sourceGroup = GroupTestUtil.addGroup();

		UnicodeProperties typeSettingsUnicodeProperties =
			sourceGroup.getTypeSettingsProperties();

		typeSettingsUnicodeProperties.put("directoryIndexingEnabled", "true");

		_groupLocalService.updateGroup(
			sourceGroup.getGroupId(), typeSettingsUnicodeProperties.toString());

		Dictionary<String, Object> properties =
			_configurationScreen.exportProperties(sourceGroup.getGroupId());

		Assert.assertEquals("true", properties.get("directoryIndexingEnabled"));

		Group targetGroup = GroupTestUtil.addGroup();

		_configurationScreen.importProperties(
			properties, targetGroup.getGroupId());

		targetGroup = _groupLocalService.getGroup(targetGroup.getGroupId());

		Assert.assertEquals(
			"true",
			targetGroup.getTypeSettingsProperty("directoryIndexingEnabled"));

		properties = _configurationScreen.exportProperties(
			sourceGroup.getGroupId());

		Assert.assertEquals("true", properties.get("directoryIndexingEnabled"));
	}

	@Test
	public void testExportProperties() throws Exception {
		Group group = GroupTestUtil.addGroup();

		Dictionary<String, Object> properties =
			_configurationScreen.exportProperties(group.getGroupId());

		Assert.assertTrue(properties.isEmpty());
	}

	@Test
	public void testImportProperties() throws Exception {
		Group group = GroupTestUtil.addGroup();

		_configurationScreen.importProperties(
			HashMapDictionaryBuilder.<String, Object>put(
				"directoryIndexingEnabled", RandomTestUtil.randomString()
			).build(),
			group.getGroupId());

		Dictionary<String, Object> properties =
			_configurationScreen.exportProperties(group.getGroupId());

		Assert.assertEquals(
			"false", properties.get("directoryIndexingEnabled"));

		_configurationScreen.importProperties(
			new HashMapDictionary<>(), group.getGroupId());

		properties = _configurationScreen.exportProperties(group.getGroupId());

		Assert.assertEquals(
			"false", properties.get("directoryIndexingEnabled"));
	}

	@Test
	public void testImportPropertiesWithStagingGroup() throws Exception {
		Group group = GroupTestUtil.addGroup();

		GroupTestUtil.enableLocalStaging(group);

		_configurationScreen.importProperties(
			HashMapDictionaryBuilder.<String, Object>put(
				"directoryIndexingEnabled", "true"
			).build(),
			group.getGroupId());

		group = _groupLocalService.getGroup(group.getGroupId());

		Assert.assertEquals(
			"true", group.getTypeSettingsProperty("directoryIndexingEnabled"));

		Group stagingGroup = group.getStagingGroup();

		Assert.assertEquals(
			"true",
			stagingGroup.getTypeSettingsProperty("directoryIndexingEnabled"));
	}

	@Inject(
		filter = "component.name=com.liferay.site.admin.web.internal.configuration.admin.display.DocumentsAndMediaSiteSettingsConfigurationScreenWrapper"
	)
	private ConfigurationScreen _configurationScreen;

	@Inject
	private GroupLocalService _groupLocalService;

}
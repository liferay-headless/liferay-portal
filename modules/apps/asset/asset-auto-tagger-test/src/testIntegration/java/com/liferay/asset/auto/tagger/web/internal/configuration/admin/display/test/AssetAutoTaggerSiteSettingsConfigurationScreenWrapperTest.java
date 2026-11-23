/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.asset.auto.tagger.web.internal.configuration.admin.display.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.asset.auto.tagger.configuration.AssetAutoTaggerConfiguration;
import com.liferay.asset.auto.tagger.configuration.AssetAutoTaggerConfigurationFactory;
import com.liferay.configuration.admin.display.ConfigurationScreen;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapDictionary;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

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
public class AssetAutoTaggerSiteSettingsConfigurationScreenWrapperTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testExportImportProperties() throws Exception {
		Group sourceGroup = GroupTestUtil.addGroup();

		UnicodeProperties typeSettingsUnicodeProperties =
			sourceGroup.getTypeSettingsProperties();

		typeSettingsUnicodeProperties.put("assetAutoTaggingEnabled", "false");

		_groupLocalService.updateGroup(
			sourceGroup.getGroupId(), typeSettingsUnicodeProperties.toString());

		Dictionary<String, Object> properties =
			_configurationScreen.exportProperties(sourceGroup.getGroupId());

		Assert.assertEquals("false", properties.get("assetAutoTaggingEnabled"));

		Group targetGroup = GroupTestUtil.addGroup();

		AssetAutoTaggerConfiguration assetAutoTaggerConfiguration =
			_assetAutoTaggerConfigurationFactory.
				getGroupAssetAutoTaggerConfiguration(targetGroup);

		Assert.assertTrue(assetAutoTaggerConfiguration.isEnabled());

		_configurationScreen.importProperties(
			properties, targetGroup.getGroupId());

		assetAutoTaggerConfiguration =
			_assetAutoTaggerConfigurationFactory.
				getGroupAssetAutoTaggerConfiguration(
					_groupLocalService.getGroup(targetGroup.getGroupId()));

		Assert.assertFalse(assetAutoTaggerConfiguration.isEnabled());

		properties = _configurationScreen.exportProperties(
			sourceGroup.getGroupId());

		Assert.assertEquals("false", properties.get("assetAutoTaggingEnabled"));
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
				"assetAutoTaggingEnabled", RandomTestUtil.randomString()
			).build(),
			group.getGroupId());

		Dictionary<String, Object> properties =
			_configurationScreen.exportProperties(group.getGroupId());

		Assert.assertEquals("false", properties.get("assetAutoTaggingEnabled"));

		_configurationScreen.importProperties(
			new HashMapDictionary<>(), group.getGroupId());

		properties = _configurationScreen.exportProperties(group.getGroupId());

		Assert.assertEquals("false", properties.get("assetAutoTaggingEnabled"));
	}

	@Inject
	private AssetAutoTaggerConfigurationFactory
		_assetAutoTaggerConfigurationFactory;

	@Inject(
		filter = "component.name=com.liferay.asset.auto.tagger.web.internal.configuration.admin.display.AssetAutoTaggerSiteSettingsConfigurationScreenWrapper"
	)
	private ConfigurationScreen _configurationScreen;

	@Inject
	private GroupLocalService _groupLocalService;

}
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mentions.web.internal.configuration.admin.display.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.configuration.admin.display.ConfigurationScreen;
import com.liferay.mentions.util.MentionsUtil;
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
public class MentionsSiteSettingsConfigurationScreenWrapperTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testExportImportProperties() throws Exception {
		Group sourceGroup = GroupTestUtil.addGroup();

		UnicodeProperties typeSettingsUnicodeProperties =
			sourceGroup.getTypeSettingsProperties();

		typeSettingsUnicodeProperties.put("mentionsEnabled", "false");

		_groupLocalService.updateGroup(
			sourceGroup.getGroupId(), typeSettingsUnicodeProperties.toString());

		Dictionary<String, Object> properties =
			_configurationScreen.exportProperties(sourceGroup.getGroupId());

		Assert.assertEquals("false", properties.get("mentionsEnabled"));

		Group targetGroup = GroupTestUtil.addGroup();

		Assert.assertTrue(
			MentionsUtil.isMentionsEnabled(targetGroup.getGroupId()));

		_configurationScreen.importProperties(
			properties, targetGroup.getGroupId());

		Assert.assertFalse(
			MentionsUtil.isMentionsEnabled(targetGroup.getGroupId()));

		properties = _configurationScreen.exportProperties(
			sourceGroup.getGroupId());

		Assert.assertEquals("false", properties.get("mentionsEnabled"));
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
				"mentionsEnabled", RandomTestUtil.randomString()
			).build(),
			group.getGroupId());

		Dictionary<String, Object> properties =
			_configurationScreen.exportProperties(group.getGroupId());

		Assert.assertEquals("false", properties.get("mentionsEnabled"));

		_configurationScreen.importProperties(
			new HashMapDictionary<>(), group.getGroupId());

		properties = _configurationScreen.exportProperties(group.getGroupId());

		Assert.assertEquals("false", properties.get("mentionsEnabled"));
	}

	@Inject(
		filter = "component.name=com.liferay.mentions.web.internal.configuration.admin.display.MentionsSiteSettingsConfigurationScreenWrapper"
	)
	private ConfigurationScreen _configurationScreen;

	@Inject
	private GroupLocalService _groupLocalService;

}
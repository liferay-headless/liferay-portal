/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.admin.web.internal.configuration.admin.display.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.configuration.admin.display.ConfigurationScreen;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.portal.kernel.exception.NoSuchGroupException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.LayoutSet;
import com.liferay.portal.kernel.model.LayoutSetPrototype;
import com.liferay.portal.kernel.service.LayoutSetLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapDictionary;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.sites.kernel.util.Sites;

import java.util.Dictionary;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Alejandro Tardín
 */
@RunWith(Arquillian.class)
public class PagesSiteSettingsConfigurationScreenWrapperTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_layoutSetPrototype = LayoutTestUtil.addLayoutSetPrototype(
			RandomTestUtil.randomString());
	}

	@Test
	public void testExportProperties() throws Exception {
		Group group = GroupTestUtil.addGroup();

		Dictionary<String, Object> properties =
			_configurationScreen.exportProperties(group.getGroupId());

		Assert.assertTrue(properties.isEmpty());

		_sites.updateLayoutSetPrototypesLinks(
			group, false, _layoutSetPrototype.getLayoutSetPrototypeId(), 0,
			true, false);

		properties = _configurationScreen.exportProperties(group.getGroupId());

		Assert.assertTrue(
			(boolean)properties.get("publicLayoutSetPrototypeLinkEnabled"));

		Group layoutSetPrototypeGroup = _layoutSetPrototype.getGroup();

		Assert.assertEquals(
			layoutSetPrototypeGroup.getExternalReferenceCode(),
			properties.get(
				"publicLayoutSetPrototypeSiteExternalReferenceCode"));
	}

	@Test
	public void testImportProperties() throws Exception {
		Group sourceGroup = GroupTestUtil.addGroup();

		_sites.updateLayoutSetPrototypesLinks(
			sourceGroup, false, _layoutSetPrototype.getLayoutSetPrototypeId(),
			0, true, false);

		Group targetGroup = GroupTestUtil.addGroup();

		_configurationScreen.importProperties(
			_configurationScreen.exportProperties(sourceGroup.getGroupId()),
			targetGroup.getGroupId());

		_assertLayoutSet(true, targetGroup);

		Group group = GroupTestUtil.addGroup();

		_configurationScreen.importProperties(
			new HashMapDictionary<>(), group.getGroupId());

		Dictionary<String, Object> properties =
			_configurationScreen.exportProperties(group.getGroupId());

		Assert.assertTrue(properties.isEmpty());

		Group layoutSetPrototypeGroup = _layoutSetPrototype.getGroup();

		_configurationScreen.importProperties(
			HashMapDictionaryBuilder.<String, Object>put(
				"publicLayoutSetPrototypeSiteExternalReferenceCode",
				layoutSetPrototypeGroup.getExternalReferenceCode()
			).build(),
			group.getGroupId());

		_assertLayoutSet(false, group);

		_configurationScreen.importProperties(
			HashMapDictionaryBuilder.<String, Object>put(
				"publicLayoutSetPrototypeLinkEnabled", true
			).build(),
			group.getGroupId());

		_assertLayoutSet(true, group);

		try {
			_configurationScreen.importProperties(
				HashMapDictionaryBuilder.<String, Object>put(
					"publicLayoutSetPrototypeSiteExternalReferenceCode",
					RandomTestUtil.randomString()
				).build(),
				group.getGroupId());

			Assert.fail();
		}
		catch (NoSuchGroupException noSuchGroupException) {
			Assert.assertNotNull(noSuchGroupException);
		}

		_assertLayoutSet(true, group);

		Group stagedGroup = GroupTestUtil.addGroup();

		GroupTestUtil.enableLocalStaging(stagedGroup);

		_configurationScreen.importProperties(
			HashMapDictionaryBuilder.<String, Object>put(
				"publicLayoutSetPrototypeLinkEnabled", true
			).put(
				"publicLayoutSetPrototypeSiteExternalReferenceCode",
				layoutSetPrototypeGroup.getExternalReferenceCode()
			).build(),
			stagedGroup.getGroupId());

		_assertLayoutSet(true, stagedGroup.getStagingGroup());
	}

	private void _assertLayoutSet(
			boolean expectedLayoutSetPrototypeLinkEnabled, Group group)
		throws Exception {

		LayoutSet layoutSet = _layoutSetLocalService.getLayoutSet(
			group.getGroupId(), false);

		Assert.assertEquals(
			expectedLayoutSetPrototypeLinkEnabled,
			layoutSet.isLayoutSetPrototypeLinkEnabled());
		Assert.assertEquals(
			_layoutSetPrototype.getUuid(),
			layoutSet.getLayoutSetPrototypeUuid());
	}

	@Inject(
		filter = "component.name=com.liferay.site.admin.web.internal.configuration.admin.display.PagesSiteSettingsConfigurationScreenWrapper"
	)
	private ConfigurationScreen _configurationScreen;

	@Inject
	private LayoutSetLocalService _layoutSetLocalService;

	@DeleteAfterTestRun
	private LayoutSetPrototype _layoutSetPrototype;

	@Inject
	private Sites _sites;

}
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.creator.openai.web.internal.configuration.admin.display.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.configuration.admin.display.ConfigurationScreen;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Veronica Gonzalez
 */
@RunWith(Arquillian.class)
public class AICreatorOpenAISiteSettingsConfigurationScreenWrapperTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();
	}

	@FeatureFlag("LPD-93786")
	@Test
	public void testIsVisible() throws Exception {
		Assert.assertTrue(_isVisible());
	}

	@FeatureFlag(enable = false, value = "LPD-93786")
	@Test
	public void testIsVisibleWhenFeatureFlagIsDisabled() throws Exception {
		Assert.assertFalse(_isVisible());
	}

	private boolean _isVisible() {
		ServiceContext serviceContext = new ServiceContext();

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setSiteGroupId(_group.getGroupId());

		mockHttpServletRequest.setAttribute(
			WebKeys.THEME_DISPLAY, themeDisplay);

		serviceContext.setRequest(mockHttpServletRequest);

		ServiceContextThreadLocal.pushServiceContext(serviceContext);

		try {
			return _configurationScreen.isVisible();
		}
		finally {
			ServiceContextThreadLocal.popServiceContext();
		}
	}

	@Inject(
		filter = "component.name=com.liferay.ai.creator.openai.web.internal.configuration.admin.display.AICreatorOpenAISiteSettingsConfigurationScreenWrapper"
	)
	private ConfigurationScreen _configurationScreen;

	@DeleteAfterTestRun
	private Group _group;

}